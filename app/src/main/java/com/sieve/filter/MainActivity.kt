package com.sieve.filter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sieve.filter.ui.SieveApp
import com.sieve.filter.ui.theme.SieveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestBatteryOptimizationExemption()
        setContent {
            val isAmoled by SieveApplication.instance.preferencesManager.isAmoledBlackMode.collectAsState()
            SieveTheme(isAmoled = isAmoled) {
                SieveApp()
            }
        }
    }

    /**
     * Requests battery optimization exemption so Android/OEM battery savers
     * don't kill the NotificationListenerService in the background.
     *
     * On ColorOS/MIUI/OxygenOS, aggressive battery management kills background
     * services unless the app is explicitly whitelisted.
     */
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (_: Exception) {
                    // Some OEMs don't support this intent — silently ignore,
                    // user can whitelist manually from Settings > Battery
                }
            }
        }
    }
}
