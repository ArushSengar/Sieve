package com.sieve.filter.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sieve.filter.data.local.entity.AppRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRuleDao {
    @Query("SELECT * FROM app_rules ORDER BY package_name ASC")
    fun getAllRules(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE package_name = :packageName LIMIT 1")
    fun getRule(packageName: String): Flow<AppRuleEntity?>

    @Query("SELECT * FROM app_rules WHERE package_name = :packageName LIMIT 1")
    suspend fun getRuleSync(packageName: String): AppRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: AppRuleEntity)

    @Delete
    suspend fun deleteRule(rule: AppRuleEntity)

    @Query("DELETE FROM app_rules WHERE package_name = :packageName")
    suspend fun deleteByPackage(packageName: String)
}
