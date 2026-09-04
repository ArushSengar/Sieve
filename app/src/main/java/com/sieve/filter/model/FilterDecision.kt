package com.sieve.filter.model

/**
 * Result of running the NotificationClassifier engine on a notification.
 *
 * @property shouldDismiss True if the notification should be cancelled via cancelNotification().
 * @property matchedRule Description of the rule or pattern that determined the decision.
 * @property reason Human-readable explanation for debugging or display in the Block Log.
 */
data class FilterDecision(
    val shouldDismiss: Boolean,
    val matchedRule: String,
    val reason: String
) {
    val isPassThrough: Boolean get() = !shouldDismiss && matchedRule == "DEFAULT_ALLOW"
    val isExplicitAllow: Boolean get() = !shouldDismiss && matchedRule != "DEFAULT_ALLOW"

    companion object {
        fun allow(matchedRule: String, reason: String = "Allowed by rule"): FilterDecision {
            return FilterDecision(shouldDismiss = false, matchedRule = matchedRule, reason = reason)
        }

        fun block(matchedRule: String, reason: String = "Blocked by rule"): FilterDecision {
            return FilterDecision(shouldDismiss = true, matchedRule = matchedRule, reason = reason)
        }

        fun passThrough(reason: String = "No spam indicators detected"): FilterDecision {
            return FilterDecision(shouldDismiss = false, matchedRule = "DEFAULT_ALLOW", reason = reason)
        }
    }
}
