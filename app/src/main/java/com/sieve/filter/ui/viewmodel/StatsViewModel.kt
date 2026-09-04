package com.sieve.filter.ui.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sieve.filter.SieveApplication
import com.sieve.filter.model.RuleMatchCount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class TopAppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val count: Int,
    val percentage: Float
)

data class StatsUiState(
    val totalBlocked: Int = 0,
    val blockedToday: Int = 0,
    val blockedThisWeek: Int = 0,
    val topApps: List<TopAppItem> = emptyList(),
    val topRules: List<RuleMatchCount> = emptyList(),
    val isLoading: Boolean = false
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as SieveApplication).repository
    private val packageManager: PackageManager = application.packageManager

    val uiState: StateFlow<StatsUiState> = combine(
        repository.getBlockLogCount(),
        repository.getTodayBlockedCount(),
        repository.getThisWeekBlockedCount(),
        repository.getTopBlockedApps(10),
        repository.getTopMatchedRules(5)
    ) { total, today, thisWeek, topApps, topRules ->
        val maxAppCount = topApps.firstOrNull()?.count ?: 1
        val mappedTopApps = topApps.map { item ->
            val appName = try {
                @Suppress("DEPRECATION")
                val appInfo = packageManager.getApplicationInfo(item.packageName, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                item.packageName
            }
            val icon = try {
                packageManager.getApplicationIcon(item.packageName)
            } catch (_: Exception) {
                null
            }
            TopAppItem(
                packageName = item.packageName,
                appName = appName,
                icon = icon,
                count = item.count,
                percentage = if (maxAppCount > 0) (item.count.toFloat() / maxAppCount.toFloat()).coerceIn(0f, 1f) else 0f
            )
        }

        StatsUiState(
            totalBlocked = total,
            blockedToday = today,
            blockedThisWeek = thisWeek,
            topApps = mappedTopApps,
            topRules = topRules,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState(isLoading = true)
    )
}
