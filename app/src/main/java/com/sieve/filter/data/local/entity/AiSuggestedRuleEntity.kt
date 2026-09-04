package com.sieve.filter.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_suggested_rules",
    indices = [
        Index(value = ["status"]),
        Index(value = ["packageName", "suggestedKeyword"])
    ]
)
data class AiSuggestedRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val suggestedKeyword: String,
    val category: String, // e.g. Financial Bait, Engagement Bait, Catalog Promo, Clickbait
    val sampleTitle: String? = null,
    val sampleText: String? = null,
    val status: String = STATUS_PENDING, // PENDING, ACCEPTED, DISMISSED
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_ACCEPTED = "ACCEPTED"
        const val STATUS_DISMISSED = "DISMISSED"

        const val CAT_FINANCIAL_BAIT = "Financial Bait"
        const val CAT_ENGAGEMENT_BAIT = "Engagement Bait"
        const val CAT_CATALOG_PROMO = "Catalog Promo"
        const val CAT_CLICKBAIT = "Clickbait"
        const val CAT_GENERIC_MARKETING = "Marketing"
    }
}
