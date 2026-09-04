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
        repository.getAllAppBlockCounts(),
        _searchQuery
    ) { installed, savedRules, blockCounts, query ->
        val ruleMap = savedRules.associate { it.packageName to it.getAppRuleMode() }
        val countMap = blockCounts.associate { it.packageName to it.count }

        val merged = installed.map { app ->
            val mode = ruleMap[app.packageName] ?: AppRuleMode.AUTO
            val count = countMap[app.packageName] ?: 0
            app.copy(mode = mode, blockCount = count)
        }.sortedWith(
            // Prioritize custom rules (ALLOW or BLOCK) over default AUTO, then by highest block count, then alphabetically
            compareBy<AppInfo> { it.mode == AppRuleMode.AUTO }
                .thenByDescending { it.blockCount }
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

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val currentPkg = getApplication<Application>().packageName
            val apps = try {
                @Suppress("DEPRECATION")
                val installed = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getInstalledApplications(
                        PackageManager.ApplicationInfoFlags.of(0L)
                    )
                } else {
                    packageManager.getInstalledApplications(0)
                }

                val filtered = installed.filter { appInfo ->
                    appInfo.packageName != currentPkg &&
                    (packageManager.getLaunchIntentForPackage(appInfo.packageName) != null ||
                     (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0)
                }.distinctBy { it.packageName }

                android.util.Log.d("AppRulesViewModel", "Found ${filtered.size} installed apps to map")

                filtered.map { appInfo ->
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
            } catch (e: Exception) {
                android.util.Log.e("AppRulesViewModel", "Error querying installed apps", e)
                emptyList()
            }

            android.util.Log.d("AppRulesViewModel", "Loaded ${apps.size} apps successfully")
            withContext(Dispatchers.Main) {
                _installedApps.value = apps
                _isLoading.value = false
            }
        }
    }
}
