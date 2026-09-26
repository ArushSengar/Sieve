package com.sieve.filter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sieve.filter.model.AppRuleMode

/**
 * Persisted per-app override mode with optional quiet-hours window.
 *
 * Spec: AppRule(package_name TEXT PRIMARY KEY, mode TEXT) — AUTO | ALLOW | BLOCK
 */
@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "mode")
    val mode: String = AppRuleMode.AUTO.name,

    @ColumnInfo(name = "quiet_hours_enabled", defaultValue = "0")
    val quietHoursEnabled: Boolean = false,

    @ColumnInfo(name = "quiet_hours_start_minutes", defaultValue = "-1")
    val quietHoursStartMinutes: Int = -1,

    @ColumnInfo(name = "quiet_hours_end_minutes", defaultValue = "-1")
    val quietHoursEndMinutes: Int = -1
) {
    fun getAppRuleMode(): AppRuleMode = AppRuleMode.fromString(mode)

    /**
     * Checks if a given time of day (in minutes from midnight, 0..1439) falls inside the quiet hours window.
     */
    fun isInsideQuietHours(currentMinutes: Int): Boolean {
        if (!quietHoursEnabled || quietHoursStartMinutes < 0 || quietHoursEndMinutes < 0) return false
        return if (quietHoursStartMinutes <= quietHoursEndMinutes) {
            // E.g. 14:00 (840) to 18:00 (1080)
            currentMinutes in quietHoursStartMinutes..quietHoursEndMinutes
        } else {
            // Overnight window, e.g. 22:00 (1320) to 07:00 (420)
            currentMinutes >= quietHoursStartMinutes || currentMinutes <= quietHoursEndMinutes
        }
    }
}
