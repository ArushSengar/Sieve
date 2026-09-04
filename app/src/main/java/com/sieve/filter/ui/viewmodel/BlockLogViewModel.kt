package com.sieve.filter.ui.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sieve.filter.SieveApplication
import com.sieve.filter.data.local.entity.BlockLogEntity
import com.sieve.filter.model.AppRuleMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BlockLogDisplayItem(
    val entity: BlockLogEntity,
    val appName: String,
    val appIcon: Drawable?
)

class BlockLogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as SieveApplication).repository
    private val packageManager: PackageManager = application.packageManager

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // App name/icon cache to avoid repetitive IPC lookups
    private val appInfoCache = mutableMapOf<String, Pair<String, Drawable?>>()

    val logs: StateFlow<List<BlockLogDisplayItem>> = combine(
        repository.getAllBlockLogs(),
        _searchQuery
    ) { logList, query ->
        val mappedList = logList.map { entity ->
            val (name, icon) = resolveAppInfo(entity.packageName)
            BlockLogDisplayItem(entity = entity, appName = name, appIcon = icon)
        }

        if (query.isBlank()) {
            mappedList
        } else {
            val lower = query.lowercase()
            mappedList.filter {
                it.appName.lowercase().contains(lower) ||
                it.entity.packageName.lowercase().contains(lower) ||
                (it.entity.title?.lowercase()?.contains(lower) == true) ||
                (it.entity.textSnippet?.lowercase()?.contains(lower) == true) ||
                it.entity.matchedRule.lowercase().contains(lower)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun alwaysAllowApp(packageName: String) {
        viewModelScope.launch {
            repository.setAppRule(packageName, AppRuleMode.ALLOW)
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteBlockLog(id)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAllBlockLogs()
        }
    }

    private fun resolveAppInfo(packageName: String): Pair<String, Drawable?> {
        appInfoCache[packageName]?.let { return it }

        val info = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val name = packageManager.getApplicationLabel(appInfo).toString()
            val icon = packageManager.getApplicationIcon(appInfo)
            Pair(name, icon)
        } catch (_: Exception) {
            Pair(packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }, null)
        }
        appInfoCache[packageName] = info
        return info
    }
}
