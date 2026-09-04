package com.sieve.filter.service

import com.sieve.filter.data.local.entity.AiSuggestedRuleEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartAiClassifierTest {

    @Test
    fun testJarSavingsBait_DetectedAsFinancialBait() {
        // Real case from user's phone screenshot
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.mysave.jar",
            title = "Save ₹10 to reach the target",
            text = "You are very close!"
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("Jar micro-savings promotional bait must be flagged as spam", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_FINANCIAL_BAIT, result.category)
        assertTrue("Extracted keyword should identify bait", result.primaryKeyword.isNotEmpty())
    }

    @Test
    fun testTruecallerEngagementBait_DetectedAsEngagementBait() {
        // Real case from user's phone screenshot
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.truecaller",
            title = "New profile views you missed this week 25 p...",
            text = "Introducing VIP Rewards 🎉 You're invited! Join..."
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("Truecaller profile views / VIP rewards must be flagged as spam", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_ENGAGEMENT_BAIT, result.category)
        assertTrue("Extracted keyword should be related to profile views or VIP rewards", result.primaryKeyword.isNotEmpty())
    }

    @Test
    fun testBewakoofCatalogPush_DetectedAsCatalogPromo() {
        // Real case from user's phone screenshot
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.bewakoof.bewakoof",
            title = "Solid Joggers, Plenty Of Colours",
            text = "Build your rotation one colour at a time 👀",
            subText = "Your Shadow..."
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("Bewakoof catalog push marketing must be flagged as spam", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_CATALOG_PROMO, result.category)
        assertTrue("Primary keyword should be extracted", result.primaryKeyword.isNotEmpty())
    }

    @Test
    fun testYouTubeClickbaitTaskWin_DetectedAsClickbait() {
        // Real case from user's phone screenshot
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.google.android.youtube",
            title = "Google Gemini Fund My Crazy: Complete 1-Min Task & Win ₹1 CRORE 💰 + ...",
            text = "DR abhishek."
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("YouTube sensational clickbait & lottery trap must be flagged as spam", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_CLICKBAIT, result.category)
        assertTrue("Keyword extracted should capture bait", result.primaryKeyword.isNotEmpty())
    }

    @Test
    fun testBankTransactionAlert_GuaranteedSafe() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.hdfc.mbank",
            title = "HDFC Bank Alert",
            text = "Rs 540.00 debited from acct ending in 4123 at Starbucks on 04-Sep. Avail bal Rs 12,450.00. Txn ID: 91823192"
        )

        val result = SmartAiClassifier.classify(payload)

        assertFalse("Real bank debits/credits must NEVER be flagged as spam", result.isSpam)
        assertTrue(result.reason.contains("safety guard"))
    }

    @Test
    fun testOtpVerificationCode_GuaranteedSafe() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.google.android.apps.authenticator2",
            title = "Google Security Alert",
            text = "Your verification code is 492104. Do not share this OTP with anyone."
        )

        val result = SmartAiClassifier.classify(payload)

        assertFalse("OTP / 2FA codes must NEVER be flagged as spam", result.isSpam)
    }

    @Test
    fun testDeliveryTracking_GuaranteedSafe() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "in.swiggy.android",
            title = "Order on the way!",
            text = "Your food order is out for delivery. Driver arriving in 5 mins."
        )

        val result = SmartAiClassifier.classify(payload)

        assertFalse("Delivery notifications must NEVER be flagged as spam", result.isSpam)
    }

    @Test
    fun testOngoingNotification_GuaranteedSafe() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.spotify.music",
            title = "Save ₹10 to reach the target playlist",
            text = "Playing track...",
            isOngoing = true
        )

        val result = SmartAiClassifier.classify(payload)

        assertFalse("Ongoing/media notifications are strictly protected", result.isSpam)
    }

    @Test
    fun testCurrencyBaitHeuristic_CatchesGenericWinMoney() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.win.cash",
            title = "Lucky Winner!",
            text = "Play now to win ₹ 500 directly in your account"
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("Generic currency bait must be detected", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_FINANCIAL_BAIT, result.category)
    }
}
