package com.sieve.filter.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

/**
 * Thread-safe preferences manager storing global filter toggles, Quiet Hours configuration,
 * Anti-Flooding / Deduplication, Log Retention Policy, and AMOLED Dark Mode preferences.
 * Exposes reactive [StateFlow]s for Compose UI and synchronous accessors for the listener service.
 */
class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isFilterEnabled = MutableStateFlow(prefs.getBoolean(KEY_FILTER_ENABLED, true))
    val isFilterEnabled: StateFlow<Boolean> = _isFilterEnabled.asStateFlow()

    private val _isQuietHoursEnabled = MutableStateFlow(prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, false))
    val isQuietHoursEnabled: StateFlow<Boolean> = _isQuietHoursEnabled.asStateFlow()

    private val _quietHoursStartHour = MutableStateFlow(prefs.getInt(KEY_QUIET_HOURS_START_HOUR, 22)) // 10 PM
    val quietHoursStartHour: StateFlow<Int> = _quietHoursStartHour.asStateFlow()

    private val _quietHoursStartMinute = MutableStateFlow(prefs.getInt(KEY_QUIET_HOURS_START_MINUTE, 0))
    val quietHoursStartMinute: StateFlow<Int> = _quietHoursStartMinute.asStateFlow()

    private val _quietHoursEndHour = MutableStateFlow(prefs.getInt(KEY_QUIET_HOURS_END_HOUR, 7)) // 7 AM
    val quietHoursEndHour: StateFlow<Int> = _quietHoursEndHour.asStateFlow()

    private val _quietHoursEndMinute = MutableStateFlow(prefs.getInt(KEY_QUIET_HOURS_END_MINUTE, 0))
    val quietHoursEndMinute: StateFlow<Int> = _quietHoursEndMinute.asStateFlow()

    private val _isDeduplicationEnabled = MutableStateFlow(prefs.getBoolean(KEY_DEDUPLICATION_ENABLED, true))
    val isDeduplicationEnabled: StateFlow<Boolean> = _isDeduplicationEnabled.asStateFlow()

    private val _logRetentionDays = MutableStateFlow(prefs.getInt(KEY_LOG_RETENTION_DAYS, 30)) // Default: 30 days
    val logRetentionDays: StateFlow<Int> = _logRetentionDays.asStateFlow()

    private val _isAmoledBlackMode = MutableStateFlow(prefs.getBoolean(KEY_AMOLED_BLACK_MODE, true))
    val isAmoledBlackMode: StateFlow<Boolean> = _isAmoledBlackMode.asStateFlow()

    private val _isAiFilterEnabled = MutableStateFlow(prefs.getBoolean(KEY_AI_FILTER_ENABLED, true))
    val isAiFilterEnabled: StateFlow<Boolean> = _isAiFilterEnabled.asStateFlow()

    fun setFilterEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FILTER_ENABLED, enabled).apply()
        _isFilterEnabled.value = enabled
    }

    fun setAiFilterEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_FILTER_ENABLED, enabled).apply()
        _isAiFilterEnabled.value = enabled
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QUIET_HOURS_ENABLED, enabled).apply()
        _isQuietHoursEnabled.value = enabled
    }

    fun setDeduplicationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEDUPLICATION_ENABLED, enabled).apply()
        _isDeduplicationEnabled.value = enabled
    }

    fun setLogRetentionDays(days: Int) {
        prefs.edit().putInt(KEY_LOG_RETENTION_DAYS, days).apply()
        _logRetentionDays.value = days
    }

    fun setAmoledBlackMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AMOLED_BLACK_MODE, enabled).apply()
        _isAmoledBlackMode.value = enabled
    }

    fun setQuietHours(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        prefs.edit()
            .putInt(KEY_QUIET_HOURS_START_HOUR, startHour)
            .putInt(KEY_QUIET_HOURS_START_MINUTE, startMinute)
            .putInt(KEY_QUIET_HOURS_END_HOUR, endHour)
            .putInt(KEY_QUIET_HOURS_END_MINUTE, endMinute)
            .apply()
        _quietHoursStartHour.value = startHour
        _quietHoursStartMinute.value = startMinute
        _quietHoursEndHour.value = endHour
        _quietHoursEndMinute.value = endMinute
    }

    /**
     * Checks if current time is within Quiet Hours.
     * During quiet hours, filtering is paused (all notifications allowed through).
     */
    fun isQuietHoursActive(): Boolean {
        if (!_isQuietHoursEnabled.value) return false

        val cal = Calendar.getInstance()
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val startMinutes = _quietHoursStartHour.value * 60 + _quietHoursStartMinute.value
        val endMinutes = _quietHoursEndHour.value * 60 + _quietHoursEndMinute.value

        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes..endMinutes
        } else {
            // Overnights, e.g. 22:00 to 07:00
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        }
    }

    /**
     * Returns true if filtering should actively run right now.
     */
    fun isFilteringActive(): Boolean {
        return _isFilterEnabled.value && !isQuietHoursActive()
    }

    companion object {
        private const val PREFS_NAME = "sieve_prefs"
        private const val KEY_FILTER_ENABLED = "filter_enabled"
        private const val KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
        private const val KEY_QUIET_HOURS_START_HOUR = "quiet_hours_start_hour"
        private const val KEY_QUIET_HOURS_START_MINUTE = "quiet_hours_start_minute"
        private const val KEY_QUIET_HOURS_END_HOUR = "quiet_hours_end_hour"
        private const val KEY_QUIET_HOURS_END_MINUTE = "quiet_hours_end_minute"
        private const val KEY_DEDUPLICATION_ENABLED = "deduplication_enabled"
        private const val KEY_LOG_RETENTION_DAYS = "log_retention_days"
        private const val KEY_AMOLED_BLACK_MODE = "amoled_black_mode"
        private const val KEY_AI_FILTER_ENABLED = "ai_filter_enabled"
    }
}
