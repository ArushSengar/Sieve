package com.sieve.filter.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.sieve.filter.SieveApplication
import com.sieve.filter.model.AppRuleMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Service that listens to incoming system notifications, classifies them via [NotificationClassifier],
 * cancels promotional spam notifications, and logs dismissed events locally to Room SQLite.
 */
class SieveNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Sieve NotificationListenerService connected and active.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Sieve NotificationListenerService disconnected.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return

        // Guard: Don't process Sieve's own notifications
        if (packageName == applicationContext.packageName) return

        val notification = sbn.notification ?: return
        val extras = notification.extras

        // Extract metadata
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val subText = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val channelId = notification.channelId
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0 || sbn.isOngoing

        Log.d(TAG, "onNotificationPosted: pkg=$packageName, title='$title', channel=$channelId, ongoing=$isOngoing")

        serviceScope.launch {
            try {
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
                    isOngoing = isOngoing
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
                } else {
                    Log.d(TAG, "✅ [ALLOWED] pkg=$packageName | title='$title' | reason=${decision.reason}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error filtering notification from $packageName", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

    companion object {
        private const val TAG = "SieveFilter"
    }
}
