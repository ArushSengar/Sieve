package com.sieve.filter.service

import com.sieve.filter.data.local.entity.KeywordRuleEntity
import com.sieve.filter.model.AppRuleMode
import com.sieve.filter.model.RuleAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationClassifierTest {

    private val defaultRules = listOf(
        KeywordRuleEntity(id = 1, packageName = null, pattern = "% off", action = RuleAction.BLOCK.name),
        KeywordRuleEntity(id = 2, packageName = null, pattern = "cashback", action = RuleAction.BLOCK.name),
        KeywordRuleEntity(id = 3, packageName = null, pattern = "flash sale", action = RuleAction.BLOCK.name),
        KeywordRuleEntity(id = 4, packageName = null, pattern = "limited time", action = RuleAction.BLOCK.name),
        KeywordRuleEntity(id = 5, packageName = null, pattern = "delivered", action = RuleAction.ALLOW.name),
        KeywordRuleEntity(id = 6, packageName = null, pattern = "out for delivery", action = RuleAction.ALLOW.name),
        KeywordRuleEntity(id = 7, packageName = null, pattern = "otp", action = RuleAction.ALLOW.name),
        KeywordRuleEntity(id = 8, packageName = null, pattern = "order confirmed", action = RuleAction.ALLOW.name)
    )

    @Test
    fun testAppRuleAlwaysAllow_BypassesSpamKeywords() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.application.zomato",
            title = "Mega Flash Sale!",
            text = "Get 60% off your next meal with cashback"
        )

        val decision = NotificationClassifier.classify(
            payload = payload,
            appRuleMode = AppRuleMode.ALLOW,
            rules = defaultRules
        )

        assertFalse("App set to ALLOW must not be dismissed", decision.shouldDismiss)
        assertEquals("AppRule: ALLOW", decision.matchedRule)
    }

    @Test
    fun testAppRuleAlwaysBlock_DismissesEvenWithoutKeywords() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.spammy.app",
            title = "Hello there",
            text = "Just checking in on you"
        )

        val decision = NotificationClassifier.classify(
            payload = payload,
            appRuleMode = AppRuleMode.BLOCK,
            rules = defaultRules
        )

        assertTrue("App set to BLOCK must be dismissed", decision.shouldDismiss)
        assertEquals("AppRule: BLOCK", decision.matchedRule)
    }

    @Test
    fun testSpamKeyword_DismissesNotification() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.shopping.app",
            title = "Weekend Special",
            text = "Enjoy 40% off on all footwear today!"
        )

        val decision = NotificationClassifier.classify(
            payload = payload,
            appRuleMode = AppRuleMode.AUTO,
            rules = defaultRules
        )

        assertTrue("Notification with 40% off should be dismissed", decision.shouldDismiss)
        assertTrue(decision.matchedRule.contains("% off"))
    }

    @Test
    fun testAllowKeyword_HasPrecedenceOverBlockKeyword() {
        // Spec test: Food delivery app says "Your order is delivered! Get 20% off your next order"
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.application.zomato",
            title = "Order Delivered!",
            text = "Your pizza was delivered. Use code PIZZA20 for 20% off next time."
        )

        val decision = NotificationClassifier.classify(
            payload = payload,
            appRuleMode = AppRuleMode.AUTO,
            rules = defaultRules
        )

        assertFalse("Notification containing 'delivered' must NOT be dismissed even if it mentions '% off'", decision.shouldDismiss)
        assertTrue(decision.matchedRule.contains("delivered"))
    }

    @Test
    fun testOtpNotification_WithCashbackPromo_IsPreserved() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.bank.app",
            title = "Bank OTP",
            text = "Your OTP is 482910 to claim your ₹50 cashback."
        )

        val decision = NotificationClassifier.classify(
            payload = payload,
            appRuleMode = AppRuleMode.AUTO,
            rules = defaultRules
        )

        assertFalse("OTP notification must not be dismissed", decision.shouldDismiss)
        assertTrue(decision.matchedRule.contains("otp"))
    }

    @Test
    fun testChannelHeuristic_DismissesPromotionalChannel() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.ecommerce.app",
            title = "New items added",
            text = "Check out our newest collection",
            channelId = "marketing_and_promotions"
        )

        val decision = NotificationClassifier.classify(
            payload = payload,
            appRuleMode = AppRuleMode.AUTO,
            rules = defaultRules
        )

        assertTrue("Promotional channel should be dismissed", decision.shouldDismiss)
        assertTrue(decision.matchedRule.contains("Channel:"))
    }

    @Test
    fun testChannelHeuristic_ImportantChannelProtected() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.ecommerce.app",
            title = "Order status updated",
            text = "Your package is moving towards the hub",
            channelId = "order_tracking"
        )

        val decision = NotificationClassifier.classify(
            payload = payload,
            appRuleMode = AppRuleMode.AUTO,
            rules = defaultRules
        )

        assertFalse("Order tracking channel must not be dismissed", decision.shouldDismiss)
    }

    @Test
    fun testOngoingNotification_NeverDismissed() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.music.player",
            title = "Playing Song",
            text = "Limited time stream",
            isOngoing = true
        )

        val decision = NotificationClassifier.classify(
            payload = payload,
            appRuleMode = AppRuleMode.AUTO,
            rules = defaultRules
        )

        assertFalse("Ongoing notification must never be dismissed", decision.shouldDismiss)
    }

    @Test
    fun testPackageScopedRule_OverridesGlobalBehavior() {
        val customRules = defaultRules + listOf(
            KeywordRuleEntity(
                id = 99,
                packageName = "com.special.app",
                pattern = "newsletter",
                action = RuleAction.BLOCK.name
            )
        )

        val payloadForTargetApp = NotificationClassifier.NotificationPayload(
            packageName = "com.special.app",
            title = "Weekly newsletter",
            text = "Here is what happened this week"
        )

        val payloadForOtherApp = NotificationClassifier.NotificationPayload(
            packageName = "com.other.app",
            title = "Weekly newsletter",
            text = "Here is what happened this week"
        )

        val decisionTarget = NotificationClassifier.classify(
            payload = payloadForTargetApp,
            appRuleMode = AppRuleMode.AUTO,
            rules = customRules
        )

        val decisionOther = NotificationClassifier.classify(
            payload = payloadForOtherApp,
            appRuleMode = AppRuleMode.AUTO,
            rules = customRules
        )

        assertTrue("Target app with scoped rule should be blocked", decisionTarget.shouldDismiss)
        assertFalse("Other app without scoped rule should pass through", decisionOther.shouldDismiss)
    }

    @Test
    fun testCaseInsensitivityAndWhitespace() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.shopping.app",
            title = "HURRY! FLASH SALE",
            text = "FLAsH  sALe is now live!"
        )

        val decision = NotificationClassifier.classify(
            payload = payload,
            appRuleMode = AppRuleMode.AUTO,
            rules = defaultRules
        )

        assertTrue("Uppercase and mixed-case keywords must match", decision.shouldDismiss)
    }
}
