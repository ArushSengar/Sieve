package com.sieve.filter.data.repository

import com.sieve.filter.data.local.SieveDatabase
import com.sieve.filter.data.local.dao.AppRuleDao
import com.sieve.filter.data.local.dao.BlockLogDao
import com.sieve.filter.data.local.dao.KeywordRuleDao
import com.sieve.filter.data.local.entity.AppRuleEntity
import com.sieve.filter.data.local.entity.BlockLogEntity
import com.sieve.filter.data.local.entity.KeywordRuleEntity
import com.sieve.filter.model.AppRuleMode
import com.sieve.filter.model.RuleAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository providing a unified, clean interface for Room database operations.
 */
class SieveRepository(
    private val appRuleDao: AppRuleDao,
    private val keywordRuleDao: KeywordRuleDao,
    private val blockLogDao: BlockLogDao
) {
    // ---------------- APP RULES ----------------

    fun getAllAppRules(): Flow<List<AppRuleEntity>> = appRuleDao.getAllRules()

    fun getAppRule(packageName: String): Flow<AppRuleEntity?> = appRuleDao.getRule(packageName)

    suspend fun getAppRuleSync(packageName: String): AppRuleEntity? = withContext(Dispatchers.IO) {
        appRuleDao.getRuleSync(packageName)
    }

    suspend fun setAppRule(packageName: String, mode: AppRuleMode) = withContext(Dispatchers.IO) {
        appRuleDao.upsertRule(AppRuleEntity(packageName = packageName, mode = mode.name))
    }

    suspend fun deleteAppRule(packageName: String) = withContext(Dispatchers.IO) {
        appRuleDao.deleteByPackage(packageName)
    }

    // ---------------- KEYWORD RULES ----------------

    fun getAllKeywordRules(): Flow<List<KeywordRuleEntity>> = keywordRuleDao.getAllRules()

    suspend fun getAllKeywordRulesSync(): List<KeywordRuleEntity> = withContext(Dispatchers.IO) {
        keywordRuleDao.getAllRulesSync()
    }

    suspend fun getRulesForPackageSync(packageName: String): List<KeywordRuleEntity> = withContext(Dispatchers.IO) {
        keywordRuleDao.getRulesForPackageSync(packageName)
    }

    suspend fun addKeywordRule(
        pattern: String,
        action: RuleAction,
        packageName: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val trimmedPattern = pattern.trim()
        if (trimmedPattern.isEmpty()) return@withContext -1L

        val entity = KeywordRuleEntity(
            packageName = packageName?.trim()?.ifEmpty { null },
            pattern = trimmedPattern,
            action = action.name
        )
        keywordRuleDao.insertRule(entity)
    }

    suspend fun deleteKeywordRule(id: Long) = withContext(Dispatchers.IO) {
        keywordRuleDao.deleteById(id)
    }

    suspend fun clearAllKeywordRules() = withContext(Dispatchers.IO) {
        keywordRuleDao.clearAll()
    }

    suspend fun resetDefaultKeywords() = withContext(Dispatchers.IO) {
        keywordRuleDao.clearAll()
        val rules = mutableListOf<KeywordRuleEntity>()
        SieveDatabase.DEFAULT_BLOCK_KEYWORDS.forEach {
            rules.add(KeywordRuleEntity(packageName = null, pattern = it, action = RuleAction.BLOCK.name))
        }
        SieveDatabase.DEFAULT_ALLOW_KEYWORDS.forEach {
            rules.add(KeywordRuleEntity(packageName = null, pattern = it, action = RuleAction.ALLOW.name))
        }
        keywordRuleDao.insertAll(rules)
    }

    // ---------------- BLOCK LOGS ----------------

    fun getAllBlockLogs(): Flow<List<BlockLogEntity>> = blockLogDao.getAllLogs()

    suspend fun logBlockedNotification(
        packageName: String,
        title: String?,
        textSnippet: String?,
        channelId: String?,
        matchedRule: String
    ): Long = withContext(Dispatchers.IO) {
        val log = BlockLogEntity(
            packageName = packageName,
            title = title,
            textSnippet = textSnippet,
            channelId = channelId,
            matchedRule = matchedRule,
            timestamp = System.currentTimeMillis()
        )
        blockLogDao.insertLog(log)
    }

    suspend fun deleteBlockLog(id: Long) = withContext(Dispatchers.IO) {
        blockLogDao.deleteById(id)
    }

    suspend fun clearAllBlockLogs() = withContext(Dispatchers.IO) {
        blockLogDao.clearAll()
    }

    fun getBlockLogCount(): Flow<Int> = blockLogDao.getCount()
}
