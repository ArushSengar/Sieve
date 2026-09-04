package com.sieve.filter.model

import androidx.room.ColumnInfo

data class AppBlockCount(
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "count")
    val count: Int
)

data class RuleMatchCount(
    @ColumnInfo(name = "matched_rule")
    val matchedRule: String,
    @ColumnInfo(name = "count")
    val count: Int
)

data class StatsSummary(
    val totalBlocked: Int = 0,
    val blockedToday: Int = 0,
    val blockedThisWeek: Int = 0,
    val topBlockedApps: List<AppBlockCount> = emptyList(),
    val topRules: List<RuleMatchCount> = emptyList()
)
