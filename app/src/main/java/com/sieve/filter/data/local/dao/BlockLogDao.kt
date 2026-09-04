package com.sieve.filter.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sieve.filter.data.local.entity.BlockLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockLogDao {
    @Query("SELECT * FROM block_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<BlockLogEntity>>

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
}
