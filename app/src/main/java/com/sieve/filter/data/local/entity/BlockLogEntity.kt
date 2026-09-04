package com.sieve.filter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Historical log of a dismissed notification.
 *
 * Spec: BlockLog(id INTEGER PK, package_name TEXT, title TEXT, text_snippet TEXT, channel_id TEXT, matched_rule TEXT, timestamp INTEGER)
 */
@Entity(tableName = "block_logs")
data class BlockLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "text_snippet")
    val textSnippet: String?,

    @ColumnInfo(name = "channel_id")
    val channelId: String?,

    @ColumnInfo(name = "matched_rule")
    val matchedRule: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
