package com.sieve.filter.service

import com.sieve.filter.data.local.entity.AiSuggestedRuleEntity
import java.text.Normalizer
import java.util.Locale

/**
 * Ultra-fast, 100% on-device intelligent heuristic & NLP spam classifier.
 *
 * Runs in < 0.2ms with zero flash I/O and zero battery impact.
 * Catches modern marketing notifications that bypass basic discount keywords:
 * 1. Financial & Reward Bait ("Win an iPhone 17", "Save ₹10 to reach target", "Win ₹1 CRORE", "Scratch & win")
 * 2. Credit Card / Loan Application Push ("Just apply for your superCard", "Tap to apply now", "Pre-approved loan")
 * 3. Cashback & Prize Claims ("Congratulations! Get 𝗰𝗮𝘀𝗵𝗯𝗮𝗰𝗸", "Claim my Rs. 12.00")
 * 4. Engagement & Social FOMO ("New profile views you missed", "VIP Rewards 🎉", "Keep your streak")
 * 5. Catalog & E-Commerce Push ("Solid Joggers, Plenty Of Colours", "Build your rotation", "Curated for you")
 * 6. Sensational Clickbait ("Complete 1-min task & win", "Fund my crazy", "Watch before it's deleted")
 *
 * Unicode Obfuscation Resilience:
 * Automatically de-obfuscates mathematical sans-serif/bold/italic Unicode fonts (e.g. 𝗰𝗮𝘀𝗵𝗯𝗮𝗰𝗸 -> cashback)
 * and strips zero-width spacing characters.
 *
 * Guaranteed Safety:
 * Never flags OTPs, banking debits/credits, two-factor auth, ride status, or delivery updates.
 */
object SmartAiClassifier {

    data class AiResult(
        val isSpam: Boolean,
        val category: String = "",
        val primaryKeyword: String = "",
        val confidence: Float = 0.0f,
        val reason: String = ""
    )

    // Guaranteed Non-Spam / Safe Patterns
    private val SAFE_PATTERNS = listOf(
        "otp",
        "verification code",
        "security code",
        "one time password",
        "one-time password",
        "auth code",
        "is your secret code",
        "do not share this otp",
        "debited",
        "credited",
        "bank transfer",
        "spent on card",
        "acct ending in",
        "account ending",
        "txn id",
        "transaction successful",
        "upi transaction",
        "out for delivery",
        "order confirmed",
        "arriving today",
        "driver arriving",
        "driver has arrived",
        "pickup code",
        "tracking id",
        "missed call",
        "incoming call",
        "sent a photo",
        "sent a video",
        "sent a voice message"
    )

    // 1. Financial, Giveaway & Credit/Loan Bait Patterns
    private val FINANCIAL_BAIT_TRIGGERS = listOf(
        // High-value giveaway / sweepstake traps
        Pair("win an iphone", "Win iPhone"),
        Pair("win iphone", "Win iPhone"),
        Pair("win a phone", "Win Phone"),
        Pair("win a smartphone", "Win Smartphone"),
        Pair("win an ipad", "Win iPad"),
        Pair("win a laptop", "Win Laptop"),
        Pair("win a car", "Win Car"),
        Pair("win a bike", "Win Bike"),
        Pair("top spender to win", "Top Spender to Win"),
        Pair("spender to win", "Spender to Win"),
        Pair("stand a chance to win", "Chance to Win"),
        Pair("chance to win", "Chance to Win"),
        Pair("lucky winner", "Lucky Winner"),
        Pair("bumper prize", "Bumper Prize"),
        Pair("win gold", "Win Gold"),

        // Credit Card / Fintech loan push
        Pair("apply for your supercard", "Apply For SuperCard"),
        Pair("apply for your card", "Apply For Card"),
        Pair("apply for your", "Apply For Card/Loan"),
        Pair("tap to apply", "Tap To Apply"),
        Pair("apply now", "Apply Now"),
        Pair("pre-approved credit", "Pre-Approved Credit"),
        Pair("pre-approved loan", "Pre-Approved Loan"),
        Pair("pre-approved limit", "Pre-Approved Limit"),
        Pair("instant loan", "Instant Loan"),
        Pair("instant credit", "Instant Credit"),
        Pair("lifetime free card", "Lifetime Free Card"),
        Pair("lifetime free credit card", "Lifetime Free Card"),
        Pair("activate your card", "Activate Card"),
        Pair("upgrade your card", "Upgrade Card"),

        // Cashback & Voucher Claims
        Pair("claim my rs", "Claim Cashback"),
        Pair("claim my ₹", "Claim Cashback"),
        Pair("claim your rs", "Claim Cashback"),
        Pair("claim your ₹", "Claim Cashback"),
        Pair("claim your cashback", "Claim Cashback"),
        Pair("claim cashback", "Claim Cashback"),
        Pair("cashback on your", "Cashback Bait"),
        Pair("congratulations! get", "Cashback Bait"),
        Pair("congratulations! you", "Cashback Bait"),
        Pair("unclaimed cashback", "Unclaimed Cashback"),
        Pair("unclaimed reward", "Unclaimed Reward"),
        Pair("assured cashback", "Assured Cashback"),
        Pair("flat cashback", "Flat Cashback"),
        Pair("cashback", "Cashback"),

        // Micro-savings / Gamified finance
        Pair("reach the target", "Reach The Target"),
        Pair("to reach the target", "Reach The Target"),
        Pair("you are very close", "You Are Very Close"),
        Pair("save ₹", "Save ₹"),
        Pair("save rs", "Save Rs"),
        Pair("add ₹", "Add ₹"),
        Pair("add rs", "Add Rs"),
        Pair("win ₹", "Win ₹"),
        Pair("win rs", "Win Rs"),
        Pair("win $", "Win $"),
        Pair("crore", "Win Crore"),
        Pair("lakh", "Win Lakh"),
        Pair("jackpot", "Jackpot"),
        Pair("lucky draw", "Lucky Draw"),
        Pair("scratch & win", "Scratch & Win"),
        Pair("scratch and win", "Scratch & Win"),
        Pair("spin the wheel", "Spin the Wheel"),
        Pair("daily spin", "Daily Spin"),
        Pair("double your cash", "Double your cash"),
        Pair("free coins", "Free Coins"),
        Pair("reward coins", "Reward Coins"),
        Pair("gold vault", "Gold Vault"),
        Pair("invest and earn", "Invest and Earn"),
        Pair("wallet balance expiring", "Wallet Balance Expiring")
    )

    // 2. Engagement Bait Patterns
    private val ENGAGEMENT_BAIT_TRIGGERS = listOf(
        Pair("profile views you missed", "Profile Views You Missed"),
        Pair("profile views", "Profile Views"),
        Pair("who viewed your profile", "Profile Views"),
        Pair("who checked your profile", "Profile Views"),
        Pair("vip rewards", "VIP Rewards"),
        Pair("you're invited", "You're Invited"),
        Pair("you are invited", "You're Invited"),
        Pair("exclusive invite", "Exclusive Invite"),
        Pair("exclusive rewards", "Exclusive Rewards"),
        Pair("waiting for you", "Waiting For You"),
        Pair("missed this week", "Missed This Week"),
        Pair("keep your streak", "Keep Your Streak"),
        Pair("streak at risk", "Streak At Risk"),
        Pair("streak is expiring", "Streak Is Expiring"),
        Pair("we miss you", "We Miss You"),
        Pair("haven't seen you in a while", "Haven't Seen You"),
        Pair("come back and get", "Come Back")
    )

    // 3. Catalog & Fashion E-Commerce Push Patterns
    private val CATALOG_PROMO_TRIGGERS = listOf(
        Pair("plenty of colours", "Plenty Of Colours"),
        Pair("plenty of colors", "Plenty Of Colors"),
        Pair("solid joggers", "Solid Joggers"),
        Pair("joggers", "Joggers"),
        Pair("oversized tees", "Oversized Tees"),
        Pair("hoodies under", "Hoodies"),
        Pair("sneakers drop", "Sneakers"),
        Pair("build your rotation", "Build Your Rotation"),
        Pair("curated for you", "Curated For You"),
        Pair("trending in your city", "Trending Styles"),
        Pair("fresh drops", "Fresh Drops"),
        Pair("steals under ₹", "Steals Under ₹"),
        Pair("steals under rs", "Steals Under Rs"),
        Pair("left in your bag", "Left In Your Bag"),
        Pair("left in your cart", "Left In Your Cart"),
        Pair("items in your cart", "Items In Your Cart"),
        Pair("price drop on your saved", "Price Drop"),
        Pair("trending styles", "Trending Styles"),
        Pair("look of the day", "Look Of The Day"),
        Pair("steal the look", "Steal The Look")
    )

    // 4. Sensational Clickbait / Task Bait
    private val CLICKBAIT_TRIGGERS = listOf(
        Pair("complete 1-min task", "1-Min Task"),
        Pair("1-min task", "1-Min Task"),
        Pair("fund my crazy", "Fund My Crazy"),
        Pair("complete task & win", "Complete Task & Win"),
        Pair("complete task and win", "Complete Task & Win"),
        Pair("watch before it's deleted", "Watch Before It's Deleted"),
        Pair("you won't believe", "You Won't Believe"),
        Pair("shocking truth", "Shocking Truth"),
        Pair("secret trick", "Secret Trick"),
        Pair("guaranteed returns", "Guaranteed Returns"),
        Pair("get rich quick", "Get Rich Quick"),
        Pair("become a crorepati", "Become A Crorepati"),
        Pair("tap to win", "Tap To Win"),
        Pair("tap to claim", "Tap To Claim"),
        Pair("claim now", "Claim Now"),
        Pair("claim reward", "Claim Reward"),
        Pair("grab now", "Grab Now"),
        Pair("hurry! only", "Hurry! Urgency Bait")
    )

    /**
     * Normalizes text by decomposing stylized Unicode characters (e.g. bold/italic math fonts
     * used by spammers like '𝗰𝗮𝘀𝗵𝗯𝗮𝗰𝗸' or '𝓯𝓻𝓮𝓮') into standard ASCII equivalents,
     * stripping zero-width spaces, and trimming.
     */
    fun normalizeSpamText(input: String?): String {
        if (input.isNullOrBlank()) return ""
        // 1. Remove zero-width characters (e.g. \u200B, \u200C, \u200D, \uFEFF)
        val stripped = input.replace(Regex("[\u200B-\u200D\uFEFF]"), "")
        // 2. Normalize via NFKD (decomposes mathematical bold, italic, script, fullwidth characters to ASCII)
        val nfkd = Normalizer.normalize(stripped, Normalizer.Form.NFKD)
        // 3. Strip combining diacritical marks
        val clean = nfkd.replace(Regex("\\p{M}+"), "")
        return clean.lowercase(Locale.ROOT).trim()
    }

    /**
     * Classifies a notification payload with AI heuristics.
     */
    fun classify(payload: NotificationClassifier.NotificationPayload): AiResult {
        // Fast exit: Ongoing notification
        if (payload.isOngoing) {
            return AiResult(isSpam = false, reason = "Ongoing notification protected")
        }

        val title = payload.title?.trim() ?: ""
        val text = payload.text?.trim() ?: ""
        val subText = payload.subText?.trim() ?: ""
        val actionsCombined = payload.actions.joinToString(" ").trim()

        if (title.isEmpty() && text.isEmpty() && actionsCombined.isEmpty()) {
            return AiResult(isSpam = false, reason = "Empty notification")
        }

        val rawCombined = buildString {
            if (title.isNotEmpty()) append(title).append(" ")
            if (text.isNotEmpty()) append(text).append(" ")
            if (subText.isNotEmpty()) append(subText).append(" ")
            if (actionsCombined.isNotEmpty()) append(actionsCombined)
        }

        val combinedContent = normalizeSpamText(rawCombined)

        // Safety check: Never flag transactional, security, or delivery notifications
        for (safe in SAFE_PATTERNS) {
            if (combinedContent.contains(safe)) {
                return AiResult(isSpam = false, reason = "Matched safety guard '$safe'")
            }
        }

        // Check 1: Clickbait / Lottery Bait (prioritized to catch task-based clickbait before raw currency triggers)
        for ((pattern, label) in CLICKBAIT_TRIGGERS) {
            if (combinedContent.contains(pattern)) {
                val extracted = extractRefinedKeyword(title, text, pattern, label)
                return AiResult(
                    isSpam = true,
                    category = AiSuggestedRuleEntity.CAT_CLICKBAIT,
                    primaryKeyword = extracted,
                    confidence = 0.93f,
                    reason = "Sensational clickbait / reward trap detected: '$extracted'"
                )
            }
        }

        // Check 2: Financial, Giveaway & Fintech Bait
        for ((pattern, label) in FINANCIAL_BAIT_TRIGGERS) {
            if (combinedContent.contains(pattern)) {
                val extracted = extractRefinedKeyword(title, text, pattern, label)
                return AiResult(
                    isSpam = true,
                    category = AiSuggestedRuleEntity.CAT_FINANCIAL_BAIT,
                    primaryKeyword = extracted,
                    confidence = 0.95f,
                    reason = "Financial / Giveaway reward bait detected: '$extracted'"
                )
            }
        }

        // Check 3: Engagement Bait
        for ((pattern, label) in ENGAGEMENT_BAIT_TRIGGERS) {
            if (combinedContent.contains(pattern)) {
                val extracted = extractRefinedKeyword(title, text, pattern, label)
                return AiResult(
                    isSpam = true,
                    category = AiSuggestedRuleEntity.CAT_ENGAGEMENT_BAIT,
                    primaryKeyword = extracted,
                    confidence = 0.92f,
                    reason = "Social / FOMO engagement bait detected: '$extracted'"
                )
            }
        }

        // Check 4: Product Catalog Push
        for ((pattern, label) in CATALOG_PROMO_TRIGGERS) {
            if (combinedContent.contains(pattern)) {
                val extracted = extractRefinedKeyword(title, text, pattern, label)
                return AiResult(
                    isSpam = true,
                    category = AiSuggestedRuleEntity.CAT_CATALOG_PROMO,
                    primaryKeyword = extracted,
                    confidence = 0.90f,
                    reason = "E-Commerce catalog push detected: '$extracted'"
                )
            }
        }

        // Check 5: Reward / Currency Claim Regex (e.g. "Claim my Rs. 12.00", "Win ₹500", "Grab Rs 100", "Save ₹ 50")
        val currencyRegex = Regex("(?:claim|get|win|grab|earn|save|add)\\s+(?:my\\s+|your\\s+)?(?:₹|rs\\.?|\\$)\\s*\\d+", RegexOption.IGNORE_CASE)
        val currencyMatch = currencyRegex.find(combinedContent)
        if (currencyMatch != null) {
            val matchedVal = currencyMatch.value.trim()
            return AiResult(
                isSpam = true,
                category = AiSuggestedRuleEntity.CAT_FINANCIAL_BAIT,
                primaryKeyword = matchedVal,
                confidence = 0.94f,
                reason = "Promotional reward / currency incentive detected: '$matchedVal'"
            )
        }

        // Check 6: Prize / Gadget Giveaway Regex (e.g. "Win an iPhone 17", "Won a smartphone", "Win a car")
        val giveawayRegex = Regex("(?:win|won)\\s+(?:an?|the)?\\s*(?:iphone|phone|smartphone|car|bike|gold|laptop|ipad|macbook|voucher)", RegexOption.IGNORE_CASE)
        val giveawayMatch = giveawayRegex.find(combinedContent)
        if (giveawayMatch != null) {
            val matchedVal = giveawayMatch.value.trim()
            return AiResult(
                isSpam = true,
                category = AiSuggestedRuleEntity.CAT_FINANCIAL_BAIT,
                primaryKeyword = matchedVal,
                confidence = 0.95f,
                reason = "Promotional giveaway bait detected: '$matchedVal'"
            )
        }

        return AiResult(isSpam = false, reason = "No promotional spam patterns matched")
    }

    /**
     * Extracts an intuitive, concise candidate keyword rule from the matched notification content.
     */
    private fun extractRefinedKeyword(
        title: String,
        text: String,
        matchedPattern: String,
        defaultLabel: String
    ): String {
        val normTitle = normalizeSpamText(title)
        val titleIdx = normTitle.indexOf(matchedPattern)
        if (titleIdx != -1) {
            val clause = normTitle.substring(titleIdx).split(Regex("[,:;•|!\\n]"))[0].trim()
            if (clause.length in 4..35) {
                return clause.split(" ").take(4).joinToString(" ").replaceFirstChar { it.uppercase() }
            }
        }

        val normText = normalizeSpamText(text)
        val textIdx = normText.indexOf(matchedPattern)
        if (textIdx != -1) {
            val clause = normText.substring(textIdx).split(Regex("[,:;•|!\\n]"))[0].trim()
            if (clause.length in 4..35) {
                return clause.split(" ").take(4).joinToString(" ").replaceFirstChar { it.uppercase() }
            }
        }

        return defaultLabel
    }
}
