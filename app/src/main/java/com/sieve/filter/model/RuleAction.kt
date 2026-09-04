package com.sieve.filter.model

/**
 * Action associated with a keyword rule.
 *
 * ALLOW: Notification containing this keyword is preserved (takes precedence over block rules).
 * BLOCK: Notification containing this keyword is dismissed as spam.
 */
enum class RuleAction {
    BLOCK,
    ALLOW;

    companion object {
        fun fromString(value: String?): RuleAction {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: BLOCK
        }
    }
}
