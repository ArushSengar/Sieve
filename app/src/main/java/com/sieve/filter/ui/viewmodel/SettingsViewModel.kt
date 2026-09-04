package com.sieve.filter.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sieve.filter.SieveApplication
import com.sieve.filter.model.RuleAction
import com.sieve.filter.service.SieveNotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class OemGuidance(
    val manufacturer: String,
    val brandName: String,
    val isKnownAggressive: Boolean,
    val instructions: List<String>,
    val dontKillMyAppUrl: String
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SieveApplication
    private val prefs = app.preferencesManager
    private val repository = app.repository

    val isFilterEnabled: StateFlow<Boolean> = prefs.isFilterEnabled
    val isAiFilterEnabled: StateFlow<Boolean> = prefs.isAiFilterEnabled
    val isQuietHoursEnabled: StateFlow<Boolean> = prefs.isQuietHoursEnabled
    val isDeduplicationEnabled: StateFlow<Boolean> = prefs.isDeduplicationEnabled
    val logRetentionDays: StateFlow<Int> = prefs.logRetentionDays
    val isAmoledBlackMode: StateFlow<Boolean> = prefs.isAmoledBlackMode
    val quietHoursStartHour: StateFlow<Int> = prefs.quietHoursStartHour
    val quietHoursStartMinute: StateFlow<Int> = prefs.quietHoursStartMinute
    val quietHoursEndHour: StateFlow<Int> = prefs.quietHoursEndHour
    val quietHoursEndMinute: StateFlow<Int> = prefs.quietHoursEndMinute

    val isServiceListening: StateFlow<Boolean> = SieveNotificationListenerService.isListening

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _isBatteryOptimizationIgnored = MutableStateFlow(false)
    val isBatteryOptimizationIgnored: StateFlow<Boolean> = _isBatteryOptimizationIgnored.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    val oemGuidance: OemGuidance = resolveOemGuidance()

    init {
        checkSystemStates()
    }

    fun checkSystemStates() {
        val context = getApplication<Application>()
        _isPermissionGranted.value = SieveNotificationListenerService.isPermissionGranted(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            _isBatteryOptimizationIgnored.value = pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } else {
            _isBatteryOptimizationIgnored.value = true
        }
    }

    fun setFilterEnabled(enabled: Boolean) {
        prefs.setFilterEnabled(enabled)
    }

    fun setAiFilterEnabled(enabled: Boolean) {
        prefs.setAiFilterEnabled(enabled)
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        prefs.setQuietHoursEnabled(enabled)
    }

    fun setDeduplicationEnabled(enabled: Boolean) {
        prefs.setDeduplicationEnabled(enabled)
    }

    fun setLogRetentionDays(days: Int) {
        prefs.setLogRetentionDays(days)
        viewModelScope.launch {
            val pruned = repository.pruneOldLogs(days)
            if (pruned > 0) {
                _actionMessage.value = "Pruned $pruned old records to free space."
            }
        }
    }

    fun setAmoledBlackMode(enabled: Boolean) {
        prefs.setAmoledBlackMode(enabled)
    }

    fun setQuietHours(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        prefs.setQuietHours(startHour, startMinute, endHour, endMinute)
    }

    fun triggerRebind() {
        val context = getApplication<Application>()
        SieveNotificationListenerService.tryRebind(context)
        _actionMessage.value = "Rebind triggered! Check service status."
    }

    fun resetDefaultKeywords() {
        viewModelScope.launch {
            repository.resetDefaultKeywords()
            _actionMessage.value = "Default keyword rules restored."
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAllBlockLogs()
            _actionMessage.value = "Block history cleared."
        }
    }

    suspend fun exportRulesJson(): String {
        val rules = repository.getAllKeywordRulesSync()
        val jsonArray = JSONArray()
        rules.forEach { rule ->
            val obj = JSONObject()
            obj.put("pattern", rule.pattern)
            obj.put("action", rule.action)
            obj.put("package", rule.packageName ?: "")
            jsonArray.put(obj)
        }
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("rules", jsonArray)
        return root.toString(2)
    }

    fun importRulesJson(jsonStr: String) {
        viewModelScope.launch {
            try {
                val root = JSONObject(jsonStr)
                val array = root.getJSONArray("rules")
                var count = 0
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val pattern = obj.getString("pattern")
                    val actionStr = obj.optString("action", "BLOCK")
                    val pkg = obj.optString("package", "").ifEmpty { null }
                    val action = if (actionStr.equals("ALLOW", ignoreCase = true)) RuleAction.ALLOW else RuleAction.BLOCK
                    repository.addKeywordRule(pattern, action, pkg)
                    count++
                }
                _actionMessage.value = "Imported $count rules successfully!"
            } catch (e: Exception) {
                _actionMessage.value = "Failed to import rules: invalid format."
            }
        }
    }

    suspend fun exportLogsCsv(): String {
        return repository.exportLogsCsv()
    }

    fun clearMessage() {
        _actionMessage.value = null
    }

    private fun resolveOemGuidance(): OemGuidance {
        val mfg = Build.MANUFACTURER.lowercase(Locale.ROOT)
        return when {
            mfg.contains("oneplus") || mfg.contains("oppo") || mfg.contains("realme") -> {
                OemGuidance(
                    manufacturer = Build.MANUFACTURER,
                    brandName = "ColorOS / OxygenOS (${Build.MANUFACTURER})",
                    isKnownAggressive = true,
                    instructions = listOf(
                        "Go to Settings > Battery > More battery settings > App battery management > Sieve.",
                        "Turn ON 'Allow background activity' and 'Allow auto-launch'.",
                        "In Recent Apps overview, tap the three dots on Sieve and select 'Lock'."
                    ),
                    dontKillMyAppUrl = "https://dontkillmyapp.com/oneplus"
                )
            }
            mfg.contains("xiaomi") || mfg.contains("redmi") || mfg.contains("poco") -> {
                OemGuidance(
                    manufacturer = Build.MANUFACTURER,
                    brandName = "MIUI / HyperOS (${Build.MANUFACTURER})",
                    isKnownAggressive = true,
                    instructions = listOf(
                        "Open Settings > Apps > Manage Apps > Sieve.",
                        "Turn ON 'Autostart'.",
                        "Under 'Battery Saver', select 'No restrictions'.",
                        "In Recent Apps screen, long-press Sieve and tap the Padlock icon."
                    ),
                    dontKillMyAppUrl = "https://dontkillmyapp.com/xiaomi"
                )
            }
            mfg.contains("samsung") -> {
                OemGuidance(
                    manufacturer = Build.MANUFACTURER,
                    brandName = "Samsung (One UI)",
                    isKnownAggressive = true,
                    instructions = listOf(
                        "Open Settings > Apps > Sieve > Battery.",
                        "Set Battery to 'Unrestricted'.",
                        "Open Device Care > Battery > Background usage limits -> Add Sieve to 'Never sleeping apps'."
                    ),
                    dontKillMyAppUrl = "https://dontkillmyapp.com/samsung"
                )
            }
            mfg.contains("vivo") || mfg.contains("iqoo") -> {
                OemGuidance(
                    manufacturer = Build.MANUFACTURER,
                    brandName = "vivo / iQOO (Funtouch OS)",
                    isKnownAggressive = true,
                    instructions = listOf(
                        "Open Settings > Battery > Background power consumption.",
                        "Find Sieve and select 'High background power consumption'.",
                        "Ensure Autostart is allowed in Settings > Applications."
                    ),
                    dontKillMyAppUrl = "https://dontkillmyapp.com/vivo"
                )
            }
            mfg.contains("huawei") || mfg.contains("honor") -> {
                OemGuidance(
                    manufacturer = Build.MANUFACTURER,
                    brandName = "Huawei / Honor (EMUI)",
                    isKnownAggressive = true,
                    instructions = listOf(
                        "Open Settings > Battery > App launch > Sieve.",
                        "Switch from 'Manage automatically' to 'Manage manually'.",
                        "Turn ON 'Auto-launch', 'Secondary launch', and 'Run in background'."
                    ),
                    dontKillMyAppUrl = "https://dontkillmyapp.com/huawei"
                )
            }
            else -> {
                OemGuidance(
                    manufacturer = Build.MANUFACTURER,
                    brandName = "Android (${Build.MANUFACTURER})",
                    isKnownAggressive = false,
                    instructions = listOf(
                        "Open Settings > Apps > Sieve > App battery usage.",
                        "Set usage to 'Unrestricted' so background filtering is never paused."
                    ),
                    dontKillMyAppUrl = "https://dontkillmyapp.com"
                )
            }
        }
    }
}
