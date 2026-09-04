package com.sieve.filter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sieve.filter.model.RuleAction

/**
 * Keyword-based filter rule.
 *
 * Spec: KeywordRule(id INTEGER PK, package_name TEXT NULL, pattern TEXT, action TEXT)
 * null package_name = global rule applicable to all apps.
 */
@Entity(tableName = "keyword_rules")
data class KeywordRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "package_name")
    val packageName: String? = null,

    @ColumnInfo(name = "pattern")
    val pattern: String,

    @ColumnInfo(name = "action")
    val action: String = RuleAction.BLOCK.name
) {
    fun getRuleAction(): RuleAction = RuleAction.fromString(action)

    val isGlobal: Boolean
        get() = packageName.isNullOrBlank()
}
