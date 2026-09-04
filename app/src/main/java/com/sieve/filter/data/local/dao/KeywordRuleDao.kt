package com.sieve.filter.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sieve.filter.data.local.entity.KeywordRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KeywordRuleDao {
    @Query("SELECT * FROM keyword_rules ORDER BY id DESC")
    fun getAllRules(): Flow<List<KeywordRuleEntity>>

    @Query("SELECT * FROM keyword_rules")
    suspend fun getAllRulesSync(): List<KeywordRuleEntity>

    @Query("SELECT * FROM keyword_rules WHERE package_name = :packageName OR package_name IS NULL")
    suspend fun getRulesForPackageSync(packageName: String): List<KeywordRuleEntity>

    @Query("SELECT * FROM keyword_rules WHERE action = :action ORDER BY id DESC")
    fun getRulesByAction(action: String): Flow<List<KeywordRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: KeywordRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<KeywordRuleEntity>)

    @Delete
    suspend fun deleteRule(rule: KeywordRuleEntity)

    @Query("DELETE FROM keyword_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM keyword_rules")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM keyword_rules")
    suspend fun count(): Int
}
