package com.sieve.filter.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sieve.filter.SieveApplication
import com.sieve.filter.data.local.entity.KeywordRuleEntity
import com.sieve.filter.model.RuleAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class KeywordFilterTab {
    ALL,
    BLOCK,
    ALLOW
}

class KeywordRulesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as SieveApplication).repository

    private val _selectedTab = MutableStateFlow(KeywordFilterTab.ALL)
    val selectedTab: StateFlow<KeywordFilterTab> = _selectedTab

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val rules: StateFlow<List<KeywordRuleEntity>> = combine(
        repository.getAllKeywordRules(),
        _selectedTab,
        _searchQuery
    ) { allRules, tab, query ->
        val tabFiltered = when (tab) {
            KeywordFilterTab.ALL -> allRules
            KeywordFilterTab.BLOCK -> allRules.filter { it.getRuleAction() == RuleAction.BLOCK }
            KeywordFilterTab.ALLOW -> allRules.filter { it.getRuleAction() == RuleAction.ALLOW }
        }

        if (query.isBlank()) {
            tabFiltered
        } else {
            val lower = query.lowercase()
            tabFiltered.filter {
                it.pattern.lowercase().contains(lower) ||
                (it.packageName?.lowercase()?.contains(lower) == true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onTabSelected(tab: KeywordFilterTab) {
        _selectedTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addRule(pattern: String, action: RuleAction, packageName: String?) {
        viewModelScope.launch {
            repository.addKeywordRule(pattern, action, packageName)
        }
    }

    fun deleteRule(id: Long) {
        viewModelScope.launch {
            repository.deleteKeywordRule(id)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetDefaultKeywords()
        }
    }
}
