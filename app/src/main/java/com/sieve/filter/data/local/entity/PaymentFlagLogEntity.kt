package com.sieve.filter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persisted log of flagged suspicious payment collect requests.
 * Stored locally only, never transmitted, prevents repetitive advisory popups for known senders.
 */
@Entity(
    tableName = "payment_flag_log",
    indices = [
        Index(value = ["sender"]),
        Index(value = ["package_name"])
    ]
)
data class PaymentFlagLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "sender")
    val sender: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "dismissed_by_user", defaultValue = "0")
    val dismissedByUser: Boolean = false
)
