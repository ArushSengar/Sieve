package com.sieve.filter.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sieve.filter.data.local.entity.BlockLogEntity
import com.sieve.filter.model.AppBlockCount
import com.sieve.filter.model.RuleMatchCount
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockLogDao {
    @Query("SELECT * FROM block_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<BlockLogEntity>>

    @Query("SELECT * FROM block_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getAllLogsSync(limit: Int = 2000): List<BlockLogEntity>

    @Query("SELECT * FROM block_logs WHERE package_name = :packageName ORDER BY timestamp DESC")
    fun getLogsForPackage(packageName: String): Flow<List<BlockLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BlockLogEntity): Long

    @Delete
    suspend fun deleteLog(log: BlockLogEntity)

    @Query("DELETE FROM block_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM block_logs")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM block_logs")
    fun getCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM block_logs WHERE timestamp >= :timestamp")
    fun getCountSince(timestamp: Long): Flow<Int>

    @Query("SELECT package_name, COUNT(*) as count FROM block_logs GROUP BY package_name ORDER BY count DESC LIMIT :limit")
    fun getTopBlockedApps(limit: Int = 10): Flow<List<AppBlockCount>>

    @Query("SELECT package_name, COUNT(*) as count FROM block_logs GROUP BY package_name")
    fun getAllAppBlockCounts(): Flow<List<AppBlockCount>>

    @Query("SELECT matched_rule, COUNT(*) as count FROM block_logs GROUP BY matched_rule ORDER BY count DESC LIMIT :limit")
    fun getTopMatchedRules(limit: Int = 5): Flow<List<RuleMatchCount>>

    @Query("DELETE FROM block_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun pruneLogsOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM block_logs WHERE package_name IN (:packageNames) AND matched_rule LIKE 'Anti-Flooding%'")
    suspend fun purgeFalsePositiveDedupLogs(packageNames: List<String>): Int
}
