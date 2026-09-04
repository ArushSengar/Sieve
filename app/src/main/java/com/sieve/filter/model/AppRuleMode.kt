package com.sieve.filter.model

/**
 * Defines the override behavior for an individual application.
 *
 * AUTO: Evaluates notification channel heuristics and keyword rules.
 * ALLOW: Bypasses the filter; all notifications from this app are kept.
 * BLOCK: Instantly dismisses all notifications from this app.
 */
enum class AppRuleMode {
    AUTO,
    ALLOW,
    BLOCK;

    companion object {
        fun fromString(value: String?): AppRuleMode {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: AUTO
        }
    }
}
