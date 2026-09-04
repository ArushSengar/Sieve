package com.sieve.filter.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sieve.filter.SieveApplication
import com.sieve.filter.model.AppInfo
import com.sieve.filter.model.AppRuleMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRulesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as SieveApplication).repository
    private val packageManager: PackageManager = application.packageManager

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())

    init {
        loadInstalledApps()
    }

    val appList: StateFlow<List<AppInfo>> = combine(
        _installedApps,
        repository.getAllAppRules(),
        _searchQuery
    ) { installed, savedRules, query ->
        val ruleMap = savedRules.associate { it.packageName to it.getAppRuleMode() }

        val merged = installed.map { app ->
            val mode = ruleMap[app.packageName] ?: AppRuleMode.AUTO
            app.copy(mode = mode)
        }.sortedWith(
            // Prioritize custom rules (ALLOW or BLOCK) over default AUTO, then alphabetically
            compareBy<AppInfo> { it.mode == AppRuleMode.AUTO }
                .thenBy { it.appName.lowercase() }
        )

        if (query.isBlank()) {
            merged
        } else {
            val lower = query.lowercase()
            merged.filter {
                it.appName.lowercase().contains(lower) || it.packageName.lowercase().contains(lower)
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

    fun setAppMode(packageName: String, mode: AppRuleMode) {
        viewModelScope.launch {
            repository.setAppRule(packageName, mode)
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = packageManager.queryIntentActivities(intent, 0)
            val currentPkg = getApplication<Application>().packageName

            val apps = resolveInfos
                .mapNotNull { it.activityInfo?.applicationInfo }
                .distinctBy { it.packageName }
                .filter { it.packageName != currentPkg }
                .map { appInfo ->
                    val name = packageManager.getApplicationLabel(appInfo).toString()
                    val icon = try {
                        packageManager.getApplicationIcon(appInfo)
                    } catch (_: Exception) {
                        null
                    }
                    AppInfo(
                        packageName = appInfo.packageName,
                        appName = name,
                        icon = icon,
                        mode = AppRuleMode.AUTO
                    )
                }

            withContext(Dispatchers.Main) {
                _installedApps.value = apps
            }
        }
    }
}
