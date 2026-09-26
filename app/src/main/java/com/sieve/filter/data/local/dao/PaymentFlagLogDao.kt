package com.sieve.filter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sieve.filter.data.local.entity.PaymentFlagLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentFlagLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PaymentFlagLogEntity): Long

    @Query("SELECT * FROM payment_flag_log WHERE sender = :sender LIMIT 1")
    suspend fun getBySender(sender: String): PaymentFlagLogEntity?

    @Query("SELECT * FROM payment_flag_log WHERE dismissed_by_user = 0 ORDER BY timestamp DESC")
    fun getActiveFlags(): Flow<List<PaymentFlagLogEntity>>

    @Query("SELECT COUNT(*) FROM payment_flag_log WHERE sender = :sender")
    suspend fun getSenderHistoryCount(sender: String): Int

    @Query("UPDATE payment_flag_log SET dismissed_by_user = 1 WHERE id = :id")
    suspend fun dismissFlag(id: Long)

    @Query("DELETE FROM payment_flag_log")
    suspend fun clearAll()
}
