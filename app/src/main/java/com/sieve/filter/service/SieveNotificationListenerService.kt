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
import java.util.Locale
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

        // Clean up legacy false-positive duplicate records for communication apps
        serviceScope.launch {
            try {
                val purged = SieveApplication.instance.repository.purgeFalsePositiveDedupLogs(
                    PROTECTED_COMMUNICATION_PACKAGES.toList() + listOf("com.amazon.dee.app")
                )
                if (purged > 0) {
                    Log.i(TAG, "🧹 Purged $purged legacy false-positive deduplication records from BlockLog.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error purging false-positive records", e)
            }
        }

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
                    processNotification(sbn, isSweep = true)
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
                processNotification(sbn, isSweep = false)
            } catch (e: Exception) {
                Log.e(TAG, "Error in onNotificationPosted for ${sbn.packageName}", e)
            }
        }
    }

    private suspend fun processNotification(sbn: StatusBarNotification, isSweep: Boolean = false) {
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

        val category = notification.category
        val isCommunicationApp = PROTECTED_COMMUNICATION_PACKAGES.contains(packageName.lowercase(Locale.ROOT))
        val isProtectedCategory = category != null && PROTECTED_NOTIFICATION_CATEGORIES.contains(category)

        // System packages are always protected from dismissal
        val isSystemPackage = packageName == "android" ||
                              packageName == "com.android.systemui" ||
                              packageName.startsWith("com.google.android.packageinstaller") ||
                              packageName.startsWith("com.android.server")

        // Active download / file transfer / progress check
        val hasActiveProgress = notification.extras?.let {
            val max = it.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
            val current = it.getInt(Notification.EXTRA_PROGRESS, 0)
            val indeterminate = it.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)
            indeterminate || (max > 0 && current < max)
        } ?: false

        // A notification is considered genuinely ongoing / protected ONLY IF:
        // - It is an ongoing call, navigation, or transport/media player
        // - It is a system package
        // - It has an active progress bar
        // - It is a communication app with foreground service / ongoing flag (e.g. Truecaller active caller ID)
        val isGenuineOngoing = (
            isSystemPackage ||
            isProtectedCategory ||
            hasActiveProgress ||
            (isCommunicationApp && ((notification.flags and Notification.FLAG_FOREGROUND_SERVICE) != 0 || (notification.flags and Notification.FLAG_NO_CLEAR) != 0)) ||
            (category == Notification.CATEGORY_CALL || category == Notification.CATEGORY_NAVIGATION || category == Notification.CATEGORY_TRANSPORT)
        ) && (
            (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0 ||
            (notification.flags and Notification.FLAG_NO_CLEAR) != 0 ||
            (notification.flags and Notification.FLAG_FOREGROUND_SERVICE) != 0 ||
            sbn.isOngoing
        )

        if (isGenuineOngoing) {
            Log.d(TAG, "✅ [ALLOWED - ONGOING/SERVICE] pkg=$packageName | Genuine ongoing/service protected")
            return
        }

        val repository = SieveApplication.instance.repository

        // 2. HIGHEST PRIORITY: Per-App Override Rule
        // If an app is set to Always Allow (e.g. WhatsApp, Truecaller, Discord), exit immediately.
        val appRuleEntity = repository.getAppRuleSync(packageName)
        val appMode = appRuleEntity?.getAppRuleMode() ?: AppRuleMode.AUTO

        if (appMode == AppRuleMode.ALLOW) {
            Log.d(TAG, "✅ [ALLOWED - APP RULE] pkg=$packageName | Set to Always Allow")
            return
        }

        val extras = notification.extras

        // Deep Multi-Layer Text Extraction
        // Extract Title (handling EXTRA_TITLE and EXTRA_TITLE_BIG)
        val rawTitle = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
        val bigTitle = extras?.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()?.trim()
        val title = when {
            !bigTitle.isNullOrBlank() && !rawTitle.isNullOrBlank() && bigTitle != rawTitle -> "$rawTitle - $bigTitle"
            !rawTitle.isNullOrBlank() -> rawTitle
            !bigTitle.isNullOrBlank() -> bigTitle
            else -> null
        }

        // Extract Body Text (handling EXTRA_TEXT, EXTRA_BIG_TEXT, and EXTRA_TEXT_LINES for InboxStyle)
        val rawText = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
        val bigText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim()
        val subText = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim()
        val summaryText = extras?.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()?.trim()
        val infoText = extras?.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString()?.trim()

        // Extract InboxStyle text lines (e.g. promo deals listed by shopping/news apps)
        val textLines = extras?.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.mapNotNull { it?.toString()?.trim() }
            ?.filter { it.isNotEmpty() }
            ?.joinToString(" ")

        val textParts = mutableListOf<String>()
        if (!rawText.isNullOrBlank()) textParts.add(rawText)
        if (!bigText.isNullOrBlank() && bigText != rawText) textParts.add(bigText)
        if (!textLines.isNullOrBlank() && textLines != rawText && textLines != bigText) textParts.add(textLines)
        if (!summaryText.isNullOrBlank() && summaryText != rawText && summaryText != subText) textParts.add(summaryText)
        if (!infoText.isNullOrBlank()) textParts.add(infoText)

        val text = if (textParts.isNotEmpty()) textParts.joinToString(" ") else null

        val actionTitles = notification.actions?.mapNotNull { it.title?.toString() } ?: emptyList()
        val channelId = notification.channelId

        // 3. Fast Exit if App is set to Always Block
        if (appMode == AppRuleMode.BLOCK) {
            cancelNotification(sbn.key)
            repository.logBlockedNotification(
                packageName = packageName,
                title = title,
                textSnippet = text,
                channelId = channelId,
                matchedRule = "AppRule: BLOCK"
            )
            Log.i(TAG, "🛡️ [BLOCKED] pkg=$packageName | Set to Always Block")
            return
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

        // 4. Fetch keyword rules relevant to this package + global rules
        val rules = repository.getRulesForPackageSync(packageName)

        // 5. Classify notification
        val payload = NotificationClassifier.NotificationPayload(
            packageName = packageName,
            title = title,
            text = text,
            subText = subText,
            channelId = channelId,
            channelName = channelName,
            isOngoing = isGenuineOngoing,
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
            return
        } else if (decision.isPassThrough && prefs.isAiFilterEnabled.value) {
            // 6. On-Device AI Classification for notifications not matched by predefined keywords
            val aiResult = SmartAiClassifier.classify(payload, prefs.isCommercialShieldEnabled.value)
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
                return
            } else {
                Log.d(TAG, "✅ [ALLOWED - AI SAFE] pkg=$packageName | title='$title' | reason=${aiResult.reason}")
            }
        } else {
            Log.d(TAG, "✅ [ALLOWED] pkg=$packageName | title='$title' | reason=${decision.reason}")
        }

        // 7. Anti-Flooding Deduplication (STRICTLY SCOPED TO MARKETING/PROMO APPS)
        // Never deduplicate communication apps (WhatsApp, Discord, Truecaller) or message/call categories.
        // Never deduplicate during active shade sweeping.
        if (!isSweep && !isCommunicationApp && !isProtectedCategory && prefs.isDeduplicationEnabled.value && !title.isNullOrBlank()) {
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
                    repository.logBlockedNotification(
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
    }

    companion object {
        private const val TAG = "SieveFilter"
        private const val DEDUP_WINDOW_MS = 10 * 60 * 1000L // 10 minutes

        // Communication packages that must NEVER be deduplicated or blocked by flood detection
        val PROTECTED_COMMUNICATION_PACKAGES = setOf(
            "com.whatsapp",
            "com.whatsapp.w4b",
            "org.telegram.messenger",
            "org.thoughtcrime.securesms", // Signal
            "com.google.android.apps.messaging",
            "com.google.android.dialer",
            "com.samsung.android.messaging",
            "com.samsung.android.dialer",
            "com.truecaller",
            "com.discord",
            "com.google.android.gm",
            "com.microsoft.office.outlook",
            "com.microsoft.teams",
            "com.slack",
            "com.skype.raider",
            "com.facebook.orca", // Messenger
            "com.instagram.android"
        )

        // Notification categories that must NEVER be touched by flood detection
        val PROTECTED_NOTIFICATION_CATEGORIES = setOf(
            Notification.CATEGORY_MESSAGE,
            Notification.CATEGORY_CALL,
            Notification.CATEGORY_EMAIL,
            Notification.CATEGORY_ALARM,
            Notification.CATEGORY_EVENT,
            Notification.CATEGORY_REMINDER,
            Notification.CATEGORY_NAVIGATION,
            Notification.CATEGORY_TRANSPORT,
            Notification.CATEGORY_WORKOUT,
            Notification.CATEGORY_SERVICE,
            Notification.CATEGORY_SYSTEM
        )

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
