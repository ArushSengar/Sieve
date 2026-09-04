package com.sieve.filter.service

import com.sieve.filter.data.local.entity.KeywordRuleEntity
import com.sieve.filter.model.AppRuleMode
import com.sieve.filter.model.FilterDecision
import com.sieve.filter.model.RuleAction
import java.util.Locale

/**
 * Pure, on-device hybrid classification engine for incoming notifications.
 *
 * Evaluation hierarchy:
 * 1. Per-App Override (ALLOW or BLOCK)
 * 2. Explicit Channel Classification (Order/Alert channels allow; Promo/Offer channels block)
 * 3. Keyword Matching:
 *    a. Allow keywords (Package-scoped, then Global) -> if match, KEEP
 *    b. Block keywords (Package-scoped, then Global) -> if match, DISMISS
 * 4. Fallback: PASS THROUGH (leave untouched)
 */
object NotificationClassifier {

    private val PROMOTIONAL_CHANNEL_KEYWORDS = listOf(
        "promo",
        "promotion",
        "promotions",
        "offer",
        "offers",
        "deal",
        "deals",
        "marketing",
        "discount",
        "discounts",
        "campaign",
        "advertisement",
        "shopping",
        "sales",
        "coupon",
        "coupons",
        "cashback",
        "rewards",
        "recommendation",
        "trending"
    )

    private val IMPORTANT_CHANNEL_KEYWORDS = listOf(
        "order",
        "orders",
        "delivery",
        "deliveries",
        "ride",
        "trip",
        "alert",
        "alerts",
        "security",
        "account",
        "otp",
        "verification",
        "transaction",
        "transactions",
        "payment",
        "transit",
        "tracking",
        "support",
        "chat",
        "call",
        "status"
    )

    data class NotificationPayload(
        val packageName: String,
        val title: String? = null,
        val text: String? = null,
        val subText: String? = null,
        val channelId: String? = null,
        val channelName: String? = null,
        val isOngoing: Boolean = false
    )

    /**
     * Evaluates a notification payload against app override mode and keyword rules.
     */
    fun classify(
        payload: NotificationPayload,
        appRuleMode: AppRuleMode = AppRuleMode.AUTO,
        rules: List<KeywordRuleEntity> = emptyList()
    ): FilterDecision {
        // 0. Scope Guard: Ongoing/persistent notifications (e.g. active call, media player, navigation)
        if (payload.isOngoing) {
            return FilterDecision.allow("ONGOING_NOTIFICATION", "Persistent/ongoing notification protected")
        }

        // 1. Per-App Override
        when (appRuleMode) {
            AppRuleMode.ALLOW -> {
                return FilterDecision.allow("AppRule: ALLOW", "Package '${payload.packageName}' is set to Always Allow")
            }
            AppRuleMode.BLOCK -> {
                return FilterDecision.block("AppRule: BLOCK", "Package '${payload.packageName}' is set to Always Block")
            }
            AppRuleMode.AUTO -> {
                // Proceed to channel and keyword classification
            }
        }

        val combinedContent = buildString {
            payload.title?.let { append(it).append(" ") }
            payload.text?.let { append(it).append(" ") }
            payload.subText?.let { append(it) }
        }.trim()

        val normalizedContent = combinedContent.lowercase(Locale.ROOT)

        // 2. Channel Introspection
        val channelIdentifier = buildString {
            payload.channelId?.let { append(it).append(" ") }
            payload.channelName?.let { append(it) }
        }.lowercase(Locale.ROOT).trim()

        var channelSuggestsBlock = false
        var matchedChannelKeyword: String? = null

        if (channelIdentifier.isNotEmpty()) {
            // Check if the channel is explicitly marked as important/transactional
            val isImportantChannel = IMPORTANT_CHANNEL_KEYWORDS.any { keyword ->
                containsWordOrPhrase(channelIdentifier, keyword)
            }

            if (!isImportantChannel) {
                // Check if channel is explicitly a promotional/marketing channel
                val promoKeyword = PROMOTIONAL_CHANNEL_KEYWORDS.firstOrNull { keyword ->
                    containsWordOrPhrase(channelIdentifier, keyword)
                }
                if (promoKeyword != null) {
                    channelSuggestsBlock = true
                    matchedChannelKeyword = promoKeyword
                }
            }
        }

        // 3. Keyword Matching
        // Partition rules
        val packageRules = rules.filter { it.packageName.equals(payload.packageName, ignoreCase = true) }
        val globalRules = rules.filter { it.isGlobal }

        val pkgAllowRules = packageRules.filter { it.getRuleAction() == RuleAction.ALLOW }
        val globalAllowRules = globalRules.filter { it.getRuleAction() == RuleAction.ALLOW }

        val pkgBlockRules = packageRules.filter { it.getRuleAction() == RuleAction.BLOCK }
        val globalBlockRules = globalRules.filter { it.getRuleAction() == RuleAction.BLOCK }

        // 3a. ALLOW keywords ALWAYS beat BLOCK keywords (e.g. "Order delivered! 20% off your next purchase")
        // Check package-specific allow rules first
        for (rule in pkgAllowRules) {
            if (matchesPattern(normalizedContent, rule.pattern)) {
                return FilterDecision.allow(
                    matchedRule = "Allow Keyword (App): ${rule.pattern}",
                    reason = "Matched package allow rule '${rule.pattern}'"
                )
            }
        }

        // Check global allow rules
        for (rule in globalAllowRules) {
            if (matchesPattern(normalizedContent, rule.pattern)) {
                return FilterDecision.allow(
                    matchedRule = "Allow Keyword: ${rule.pattern}",
                    reason = "Matched global allow rule '${rule.pattern}'"
                )
            }
        }

        // If channel explicitly indicated promo and no allow keyword superseded it, block!
        if (channelSuggestsBlock && matchedChannelKeyword != null) {
            return FilterDecision.block(
                matchedRule = "Channel: $matchedChannelKeyword",
                reason = "Channel '${payload.channelId}' matched promotional keyword '$matchedChannelKeyword'"
            )
        }

        // 3b. BLOCK keywords
        // Check package-specific block rules first
        for (rule in pkgBlockRules) {
            if (matchesPattern(normalizedContent, rule.pattern)) {
                return FilterDecision.block(
                    matchedRule = "Keyword (App): ${rule.pattern}",
                    reason = "Matched package block rule '${rule.pattern}'"
                )
            }
        }

        // Check global block rules
        for (rule in globalBlockRules) {
            if (matchesPattern(normalizedContent, rule.pattern)) {
                return FilterDecision.block(
                    matchedRule = "Keyword: ${rule.pattern}",
                    reason = "Matched global block rule '${rule.pattern}'"
                )
            }
        }

        // 4. Default: Leave untouched
        return FilterDecision.passThrough("Notification passed all checks without matching spam indicators")
    }

    private fun matchesPattern(content: String, pattern: String): Boolean {
        if (content.isEmpty() || pattern.isEmpty()) return false
        val cleanPattern = pattern.lowercase(Locale.ROOT).trim()

        // If pattern starts with "regex:", treat as regular expression
        return if (cleanPattern.startsWith("regex:")) {
            try {
                val regexString = cleanPattern.removePrefix("regex:").trim()
                Regex(regexString, RegexOption.IGNORE_CASE).containsMatchIn(content)
            } catch (_: Exception) {
                content.contains(cleanPattern)
            }
        } else {
            // Substring search with case-insensitivity
            content.contains(cleanPattern)
        }
    }

    private fun containsWordOrPhrase(text: String, wordOrPhrase: String): Boolean {
        return text.contains(wordOrPhrase)
    }
}
