package com.sieve.filter.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import com.sieve.filter.SieveApplication
import com.sieve.filter.model.AppRuleMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Service that listens to incoming system notifications, classifies them via [NotificationClassifier],
 * cancels promotional spam notifications, and logs dismissed events locally to Room SQLite.
 *
 * Includes OEM rebind resilience (ColorOS, MIUI, OxygenOS), health state monitoring,
 * and Anti-Flooding / Duplicate suppression.
 */
class SieveNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        _isListening.value = true
        Log.i(TAG, "Sieve NotificationListenerService connected and actively filtering.")
        sweepActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (instance == this) instance = null
        _isListening.value = false
        Log.w(TAG, "Sieve NotificationListenerService disconnected! Attempting rebind...")
        tryRebind(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) instance = null
        _isListening.value = false
    }

    /**
     * Sweeps all currently active notifications in the status bar/shade and dismisses any spam.
     */
    fun sweepActiveNotifications() {
        val active = try {
            activeNotifications
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query activeNotifications", e)
            null
        } ?: return

        Log.i(TAG, "🧹 Sweeping ${active.size} active notifications in shade...")
        serviceScope.launch {
            for (sbn in active) {
                try {
                    processNotification(sbn)
                } catch (e: Exception) {
                    Log.e(TAG, "Error sweeping active notification: ${sbn.packageName}", e)
                }
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        serviceScope.launch {
            try {
                processNotification(sbn)
            } catch (e: Exception) {
                Log.e(TAG, "Error in onNotificationPosted for ${sbn.packageName}", e)
            }
        }
    }

    private suspend fun processNotification(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: return

        // Guard: Don't process Sieve's own notifications
        if (packageName == applicationContext.packageName) return

        val prefs = SieveApplication.instance.preferencesManager

        // Check if filtering is active (Global toggle + Quiet Hours check)
        if (!prefs.isFilteringActive()) {
            Log.d(TAG, "Filtering currently paused (Global toggle disabled or Quiet Hours active). Passing notification.")
            return
        }

        val notification = sbn.notification ?: return
        val extras = notification.extras

        // Extract metadata
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val rawText = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val subText = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        val text = if (!bigText.isNullOrBlank() && bigText != rawText) {
            if (rawText.isNullOrBlank()) bigText else "$rawText $bigText"
        } else {
            rawText ?: bigText
        }

        val actionTitles = notification.actions?.mapNotNull { it.title?.toString() } ?: emptyList()
        val channelId = notification.channelId
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0 || sbn.isOngoing

        // Guard: Never touch ongoing notifications (music, call, navigation)
        if (isOngoing) return

        // Anti-Flooding / Deduplication check
        if (prefs.isDeduplicationEnabled.value && !title.isNullOrBlank()) {
            val dedupKey = "$packageName|${title.trim()}|${text?.trim() ?: ""}"
            val now = System.currentTimeMillis()
            val isDuplicate = synchronized(recentNotificationTimestamps) {
                val lastSeen = recentNotificationTimestamps[dedupKey]
                if (lastSeen != null && (now - lastSeen) < DEDUP_WINDOW_MS) {
                    true
                } else {
                    recentNotificationTimestamps[dedupKey] = now
                    false
                }
            }

            if (isDuplicate) {
                cancelNotification(sbn.key)
                try {
                    SieveApplication.instance.repository.logBlockedNotification(
                        packageName = packageName,
                        title = title,
                        textSnippet = text,
                        channelId = channelId,
                        matchedRule = "Anti-Flooding: Duplicate (< 10m)"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error logging dedup block", e)
                }
                Log.i(TAG, "🛡️ [DEDUP BLOCKED] pkg=$packageName | title='$title' (Duplicate within 10m)")
                return
            }
        }

        // Channel introspection via RankingMap (requires API 31+)
        val ranking = Ranking()
        var channelName: String? = null
        @Suppress("DEPRECATION")
        val rankingMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            currentRanking
        } else {
            null
        }
        if (rankingMap != null && rankingMap.getRanking(sbn.key, ranking)) {
            channelName = ranking.channel?.name?.toString()
        }

        val repository = SieveApplication.instance.repository

        // 1. Fetch current app rule for this package
        val appRuleEntity = repository.getAppRuleSync(packageName)
        val appMode = appRuleEntity?.getAppRuleMode() ?: AppRuleMode.AUTO

        // 2. Fetch keyword rules relevant to this package + global rules
        val rules = repository.getRulesForPackageSync(packageName)

        // 3. Classify notification
        val payload = NotificationClassifier.NotificationPayload(
            packageName = packageName,
            title = title,
            text = text,
            subText = subText,
            channelId = channelId,
            channelName = channelName,
            isOngoing = isOngoing,
            actions = actionTitles
        )

        val decision = NotificationClassifier.classify(
            payload = payload,
            appRuleMode = appMode,
            rules = rules
        )

        if (decision.shouldDismiss) {
            // Cancel notification immediately
            cancelNotification(sbn.key)

            // Record in Room SQLite BlockLog
            repository.logBlockedNotification(
                packageName = packageName,
                title = title,
                textSnippet = text,
                channelId = channelId,
                matchedRule = decision.matchedRule
            )

            Log.i(TAG, "🛡️ [BLOCKED] pkg=$packageName | title='$title' | matched=${decision.matchedRule}")
        } else if (decision.isPassThrough && prefs.isAiFilterEnabled.value) {
            // 4. On-Device AI Classification for notifications not matched by predefined keywords
            val aiResult = SmartAiClassifier.classify(payload)
            if (aiResult.isSpam) {
                cancelNotification(sbn.key)

                val aiRuleTag = "AI: ${aiResult.category} (${aiResult.primaryKeyword})"
                repository.logBlockedNotification(
                    packageName = packageName,
                    title = title,
                    textSnippet = text,
                    channelId = channelId,
                    matchedRule = aiRuleTag
                )

                repository.recordAiSuggestion(
                    packageName = packageName,
                    suggestedKeyword = aiResult.primaryKeyword,
                    category = aiResult.category,
                    sampleTitle = title,
                    sampleText = text
                )

                Log.i(TAG, "🤖 [AI BLOCKED] pkg=$packageName | keyword='${aiResult.primaryKeyword}' | cat=${aiResult.category} | title='$title'")
            } else {
                Log.d(TAG, "✅ [ALLOWED - AI SAFE] pkg=$packageName | title='$title' | reason=${aiResult.reason}")
            }
        } else {
            Log.d(TAG, "✅ [ALLOWED] pkg=$packageName | title='$title' | reason=${decision.reason}")
        }
    }

    companion object {
        private const val TAG = "SieveFilter"
        private const val DEDUP_WINDOW_MS = 10 * 60 * 1000L // 10 minutes

        @Volatile
        private var instance: SieveNotificationListenerService? = null

        /**
         * Triggers an active status bar sweep from any component (e.g. MainActivity on resume).
         */
        fun sweepActiveNotificationsInstance() {
            instance?.sweepActiveNotifications()
        }

        // LRU Cache for notification deduplication (max 100 entries)
        private val recentNotificationTimestamps = object : LinkedHashMap<String, Long>(100, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>?): Boolean {
                return size > 100
            }
        }

        private val _isListening = MutableStateFlow(false)
        val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

        /**
         * Checks if the Notification Listener permission is granted by the user.
         */
        fun isPermissionGranted(context: Context): Boolean {
            val pkgName = context.packageName
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":".toRegex())
                for (name in names) {
                    val cn = ComponentName.unflattenFromString(name)
                    if (cn != null && TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
            return false
        }

        /**
         * Triggers service rebind and applies the OEM component-toggle workaround.
         * Solves the notorious issue on ColorOS/MIUI/Samsung where NotificationListenerService
         * gets silently disconnected or fails to rebind after boot.
         */
        fun tryRebind(context: Context) {
            if (!isPermissionGranted(context)) {
                Log.w(TAG, "Cannot rebind: Notification listener permission not granted.")
                return
            }

            // 1. Android N+ explicit rebind API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    requestRebind(ComponentName(context, SieveNotificationListenerService::class.java))
                    Log.i(TAG, "requestRebind() invoked")
                } catch (e: Exception) {
                    Log.w(TAG, "requestRebind() failed: ${e.message}")
                }
            }

            // 2. OEM component-toggle trick to force NotificationManagerService to cycle binding
            try {
                val pm = context.packageManager
                val component = ComponentName(context, SieveNotificationListenerService::class.java)
                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.i(TAG, "OEM component toggle trick applied successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to apply component toggle trick", e)
            }
        }
    }
}
