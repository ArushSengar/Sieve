package com.sieve.filter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sieve.filter.data.local.entity.AiSuggestedRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiSuggestedRuleDao {

    @Query("SELECT * FROM ai_suggested_rules WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingSuggestions(): Flow<List<AiSuggestedRuleEntity>>

    @Query("SELECT COUNT(*) FROM ai_suggested_rules WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT * FROM ai_suggested_rules WHERE id = :id")
    suspend fun getById(id: Long): AiSuggestedRuleEntity?

    @Query("SELECT * FROM ai_suggested_rules WHERE packageName = :packageName AND LOWER(suggestedKeyword) = LOWER(:keyword) LIMIT 1")
    suspend fun findExisting(packageName: String, keyword: String): AiSuggestedRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(suggestion: AiSuggestedRuleEntity): Long

    @Query("UPDATE ai_suggested_rules SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM ai_suggested_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ai_suggested_rules")
    suspend fun clearAll()
}
