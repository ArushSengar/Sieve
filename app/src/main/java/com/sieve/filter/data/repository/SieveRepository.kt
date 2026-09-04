package com.sieve.filter.data.repository

import com.sieve.filter.data.local.SieveDatabase
import com.sieve.filter.data.local.dao.AiSuggestedRuleDao
import com.sieve.filter.data.local.dao.AppRuleDao
import com.sieve.filter.data.local.dao.BlockLogDao
import com.sieve.filter.data.local.dao.KeywordRuleDao
import com.sieve.filter.data.local.entity.AiSuggestedRuleEntity
import com.sieve.filter.data.local.entity.AppRuleEntity
import com.sieve.filter.data.local.entity.BlockLogEntity
import com.sieve.filter.data.local.entity.KeywordRuleEntity
import com.sieve.filter.model.AppBlockCount
import com.sieve.filter.model.AppRuleMode
import com.sieve.filter.model.RuleAction
import com.sieve.filter.model.RuleMatchCount
import com.sieve.filter.service.NotificationClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Repository providing a unified, clean, and ultra-battery-efficient interface for Room database operations.
 *
 * Energy & Performance Optimizations:
 * - In-Memory RAM Caching: [appRulesCache] and [keywordRulesCache] serve live notification queries
 *   without triggering flash storage read I/O or waking SQLite mutexes (< 0.1ms access time).
 * - Automatic DB Pruning: Truncates stale logs based on user retention settings to keep Room SQLite lean.
 */
class SieveRepository(
    private val appRuleDao: AppRuleDao,
    private val keywordRuleDao: KeywordRuleDao,
    private val blockLogDao: BlockLogDao,
    private val aiSuggestedRuleDao: AiSuggestedRuleDao,
    scope: CoroutineScope? = null
) {
    // In-memory RAM caches for 0-disk-read notification processing
    private val appRulesCache = ConcurrentHashMap<String, AppRuleEntity>()
    private val keywordRulesCache = CopyOnWriteArrayList<KeywordRuleEntity>()

    init {
        scope?.let { s ->
            appRuleDao.getAllRules().onEach { rules ->
                appRulesCache.clear()
                rules.forEach { appRulesCache[it.packageName] = it }
            }.launchIn(s)

            keywordRuleDao.getAllRules().onEach { rules ->
                keywordRulesCache.clear()
                keywordRulesCache.addAll(rules)
                NotificationClassifier.clearRegexCache()
            }.launchIn(s)
        }
    }

    // ---------------- APP RULES ----------------

    fun getAllAppRules(): Flow<List<AppRuleEntity>> = appRuleDao.getAllRules()

    fun getAppRule(packageName: String): Flow<AppRuleEntity?> = appRuleDao.getRule(packageName)

    suspend fun getAppRuleSync(packageName: String): AppRuleEntity? {
        val cached = appRulesCache[packageName]
        if (cached != null) return cached
        return withContext(Dispatchers.IO) {
            val rule = appRuleDao.getRuleSync(packageName)
            if (rule != null) appRulesCache[packageName] = rule
            rule
        }
    }

    suspend fun setAppRule(packageName: String, mode: AppRuleMode) = withContext(Dispatchers.IO) {
        val entity = AppRuleEntity(packageName = packageName, mode = mode.name)
        appRulesCache[packageName] = entity
        appRuleDao.upsertRule(entity)
    }

    suspend fun deleteAppRule(packageName: String) = withContext(Dispatchers.IO) {
        appRulesCache.remove(packageName)
        appRuleDao.deleteByPackage(packageName)
    }

    // ---------------- KEYWORD RULES ----------------

    fun getAllKeywordRules(): Flow<List<KeywordRuleEntity>> = keywordRuleDao.getAllRules()

    suspend fun getAllKeywordRulesSync(): List<KeywordRuleEntity> = withContext(Dispatchers.IO) {
        if (keywordRulesCache.isNotEmpty()) keywordRulesCache.toList()
        else {
            val rules = keywordRuleDao.getAllRulesSync()
            keywordRulesCache.clear()
            keywordRulesCache.addAll(rules)
            rules
        }
    }

    suspend fun getRulesForPackageSync(packageName: String): List<KeywordRuleEntity> {
        if (keywordRulesCache.isNotEmpty()) {
            return keywordRulesCache.filter { it.isGlobal || it.packageName.equals(packageName, ignoreCase = true) }
        }
        return withContext(Dispatchers.IO) {
            val rules = keywordRuleDao.getAllRulesSync()
            keywordRulesCache.clear()
            keywordRulesCache.addAll(rules)
            rules.filter { it.isGlobal || it.packageName.equals(packageName, ignoreCase = true) }
        }
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
        NotificationClassifier.clearRegexCache()
        keywordRuleDao.insertRule(entity)
    }

    suspend fun deleteKeywordRule(id: Long) = withContext(Dispatchers.IO) {
        NotificationClassifier.clearRegexCache()
        keywordRuleDao.deleteById(id)
    }

    suspend fun clearAllKeywordRules() = withContext(Dispatchers.IO) {
        NotificationClassifier.clearRegexCache()
        keywordRulesCache.clear()
        keywordRuleDao.clearAll()
    }

    suspend fun resetDefaultKeywords() = withContext(Dispatchers.IO) {
        NotificationClassifier.clearRegexCache()
        keywordRulesCache.clear()
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

    // ---------------- AUTO-PRUNING & LOG RETENTION ----------------

    /**
     * Deletes log records older than [days] to keep SQLite size compact and reduce flash wear.
     */
    suspend fun pruneOldLogs(days: Int): Int = withContext(Dispatchers.IO) {
        if (days <= 0) return@withContext 0
        val cutoff = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000L)
        blockLogDao.pruneLogsOlderThan(cutoff)
    }

    /**
     * Exports block logs as a formatted CSV string.
     */
    suspend fun exportLogsCsv(): String = withContext(Dispatchers.IO) {
        val allLogs = blockLogDao.getAllLogsSync(2000)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
        val sb = StringBuilder("ID,Package,Title,Message,Channel,MatchedRule,Timestamp,DateTime\n")
        allLogs.forEach { log ->
            val cleanTitle = (log.title ?: "").replace("\"", "\"\"").replace("\n", " ")
            val cleanSnippet = (log.textSnippet ?: "").replace("\"", "\"\"").replace("\n", " ")
            val cleanChannel = (log.channelId ?: "").replace("\"", "\"\"")
            val cleanRule = log.matchedRule.replace("\"", "\"\"")
            val dateStr = dateFormat.format(Date(log.timestamp))
            sb.append("${log.id},\"${log.packageName}\",\"$cleanTitle\",\"$cleanSnippet\",\"$cleanChannel\",\"$cleanRule\",${log.timestamp},\"$dateStr\"\n")
        }
        sb.toString()
    }

    // ---------------- STATISTICS ----------------

    fun getTodayBlockedCount(): Flow<Int> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return blockLogDao.getCountSince(cal.timeInMillis)
    }

    fun getThisWeekBlockedCount(): Flow<Int> {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return blockLogDao.getCountSince(cal.timeInMillis)
    }

    fun getTopBlockedApps(limit: Int = 10): Flow<List<AppBlockCount>> =
        blockLogDao.getTopBlockedApps(limit)

    fun getAllAppBlockCounts(): Flow<List<AppBlockCount>> =
        blockLogDao.getAllAppBlockCounts()

    fun getTopMatchedRules(limit: Int = 5): Flow<List<RuleMatchCount>> =
        blockLogDao.getTopMatchedRules(limit)

    // ---------------- AI SUGGESTED RULES ----------------

    fun getPendingAiSuggestions(): Flow<List<AiSuggestedRuleEntity>> =
        aiSuggestedRuleDao.getPendingSuggestions()

    fun getPendingAiCount(): Flow<Int> =
        aiSuggestedRuleDao.getPendingCount()

    suspend fun recordAiSuggestion(
        packageName: String,
        suggestedKeyword: String,
        category: String,
        sampleTitle: String?,
        sampleText: String?
    ): Long = withContext(Dispatchers.IO) {
        val trimmed = suggestedKeyword.trim()
        if (trimmed.isEmpty()) return@withContext -1L

        // Skip if this exact keyword already exists in active rules
        val alreadyHasRule = keywordRulesCache.any {
            it.pattern.equals(trimmed, ignoreCase = true) &&
                    (it.isGlobal || it.packageName.equals(packageName, ignoreCase = true))
        }
        if (alreadyHasRule) return@withContext -1L

        // Skip if already in pending suggestions for this app
        val existing = aiSuggestedRuleDao.findExisting(packageName, trimmed)
        if (existing != null) return@withContext existing.id

        val entity = AiSuggestedRuleEntity(
            packageName = packageName,
            suggestedKeyword = trimmed,
            category = category,
            sampleTitle = sampleTitle,
            sampleText = sampleText,
            status = AiSuggestedRuleEntity.STATUS_PENDING
        )
        aiSuggestedRuleDao.insertSuggestion(entity)
    }

    suspend fun acceptAiSuggestion(id: Long, asGlobal: Boolean = true): Long = withContext(Dispatchers.IO) {
        val suggestion = aiSuggestedRuleDao.getById(id) ?: return@withContext -1L

        // Add as permanent BLOCK keyword rule
        val ruleId = addKeywordRule(
            pattern = suggestion.suggestedKeyword,
            action = RuleAction.BLOCK,
            packageName = if (asGlobal) null else suggestion.packageName
        )

        // Mark suggestion as accepted
        aiSuggestedRuleDao.updateStatus(id, AiSuggestedRuleEntity.STATUS_ACCEPTED)
        ruleId
    }

    suspend fun dismissAiSuggestion(id: Long) = withContext(Dispatchers.IO) {
        aiSuggestedRuleDao.updateStatus(id, AiSuggestedRuleEntity.STATUS_DISMISSED)
    }

    suspend fun deleteAiSuggestion(id: Long) = withContext(Dispatchers.IO) {
        aiSuggestedRuleDao.deleteById(id)
    }

    suspend fun clearAllAiSuggestions() = withContext(Dispatchers.IO) {
        aiSuggestedRuleDao.clearAll()
    }
}
