package com.sieve.filter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sieve.filter.service.SieveNotificationListenerService

/**
 * Rebinds [SieveNotificationListenerService] on system boot or app update to prevent OEM sleep drops.
 * Solves the issue where aggressive OEM battery killers (MIUI/HyperOS, ColorOS, FuntouchOS)
 * fail to restart the notification listener service after a phone reboot.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SieveBootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i(TAG, "Received broadcast action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            if (SieveNotificationListenerService.isPermissionGranted(context)) {
                Log.i(TAG, "Notification listener permission granted. Invoking tryRebind on boot/update.")
                SieveNotificationListenerService.tryRebind(context)
            } else {
                Log.w(TAG, "Notification listener permission not granted. Skipping rebind.")
            }
        }
    }
}
