package com.sieve.filter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sieve.filter.model.AppRuleMode

/**
 * Persisted per-app override mode.
 *
 * Spec: AppRule(package_name TEXT PRIMARY KEY, mode TEXT) — AUTO | ALLOW | BLOCK
 */
@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "mode")
    val mode: String = AppRuleMode.AUTO.name
) {
    fun getAppRuleMode(): AppRuleMode = AppRuleMode.fromString(mode)
}
