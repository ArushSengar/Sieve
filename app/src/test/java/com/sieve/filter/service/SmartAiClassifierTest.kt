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
    fun testGPayPeerToPeerPayment_GuaranteedSafe() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.google.android.apps.nbu.paisa.user",
            title = "NAVEEN KUMAR paid you ₹501.00",
            text = "na",
            channelId = "default"
        )

        val result = SmartAiClassifier.classify(payload)

        assertFalse("Real GPay peer-to-peer payment transfers must NEVER be flagged as spam", result.isSpam)
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

    @Test
    fun testSuperMoneyIphoneGiveaway_DetectedAsFinancialBait() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "money.super.payments",
            title = "Win an iPhone 17! 🤩 📱",
            text = "Just apply for your superCard & become our top spender to win. Tap to apply now! 🚀",
            channelId = "moe_default_channel"
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("super.money iPhone giveaway & card apply trap must be flagged as spam", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_FINANCIAL_BAIT, result.category)
        assertTrue("Extracted keyword should identify bait", result.primaryKeyword.isNotEmpty())
    }

    @Test
    fun testNaviUnicodeCashbackBait_DetectedAsFinancialBait() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.naviapp",
            title = "Rs. 12.00 🎉",
            text = "Congratulations! Get 𝗰𝗮𝘀𝗵𝗯𝗮𝗰𝗸 on your prepaid recharge.",
            channelId = "navi.channel",
            actions = listOf("Claim my Rs. 12.00 ✅")
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("Navi stylized Unicode cashback & claim bait must be flagged as spam", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_FINANCIAL_BAIT, result.category)
        assertTrue("Extracted keyword should identify cashback/claim", result.primaryKeyword.isNotEmpty())
    }

    @Test
    fun testNormalizeSpamText_DeobfuscatesUnicodeMathFonts() {
        val stylized = "Congratulations! Get 𝗰𝗮𝘀𝗵𝗯𝗮𝗰𝗸 on your prepaid recharge."
        val normalized = SmartAiClassifier.normalizeSpamText(stylized)
        assertTrue("Stylized bold math font must be normalized to standard ASCII", normalized.contains("cashback"))
    }

    @Test
    fun testJarStickyCashbackBait_DetectedAsFinancialBait() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.jar.app",
            title = "Arush Sengar, CASHBACK OFFER",
            text = "Win Cashback up to ₹5,000 on your daily savings! Tap to claim."
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("Jar sticky cashback marketing must be caught as spam", result.isSpam)
        assertTrue(result.category == AiSuggestedRuleEntity.CAT_FINANCIAL_BAIT || result.category == AiSuggestedRuleEntity.CAT_CLICKBAIT)
        assertTrue(result.primaryKeyword.isNotEmpty())
    }

    @Test
    fun testNaviReferralBounty_DetectedAsReferral() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.naviapp",
            title = "Refer & Earn Rs. 30",
            text = "Refer a friend, get Rs. 30 each for your next buy. Invite now!"
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("Navi referral bribe must be detected as spam", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_REFERRAL, result.category)
    }

    @Test
    fun testDominosFreePizzaLoyalty_DetectedAsLoyaltyFreebie() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.dominant.dominos",
            title = "Fan of Free Pizzas?",
            text = "Your Cheesy Rewards Journey has started. Collect points for free pizza!"
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("Domino's free pizza loyalty bait must be detected as spam", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_LOYALTY_FREEBIE, result.category)
    }

    @Test
    fun testFlipkartCoolestDeals_BlockedByCommercialShieldOrPromo() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.flipkart.android",
            title = "Grab the coolest deals now!",
            text = "Electronics up to 70% off. Limited time flash sale."
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertTrue("Flipkart deal broadcast must be intercepted", result.isSpam)
        assertTrue(result.category == AiSuggestedRuleEntity.CAT_COMMERCIAL_PROMO || result.category == AiSuggestedRuleEntity.CAT_CATALOG_PROMO)
    }

    @Test
    fun testLinkedInDailyPuzzle_DetectedAsGamification() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.linkedin.android",
            title = "Zip #550: How will you play?",
            text = "A new puzzle is ready. Beat your daily record!"
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("LinkedIn daily game/puzzle retention ping must be detected as spam", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_GAMIFICATION, result.category)
    }

    @Test
    fun testFlipkartGenuineOrderDelivered_GuaranteedSafe() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.flipkart.android",
            title = "Flipkart: Order Delivered",
            text = "Your order OD9281726312 containing Wireless Headphones has been delivered. Thank you for shopping!"
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertFalse("Genuine e-commerce delivered alerts must NEVER be flagged as spam", result.isSpam)
    }

    @Test
    fun testDominosPizzaOutForDelivery_GuaranteedSafe() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.dominant.dominos",
            title = "Domino's: Order Confirmed & Baking",
            text = "Your Margherita pizza is baking in the oven. Rider will be out for delivery shortly."
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertFalse("Genuine food order cooking / out for delivery notifications must NEVER be flagged as spam", result.isSpam)
    }

    @Test
    fun testBlinkitDoorstepDeliveryPromo_BlockedByCommercialShield() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.grofers.customerapp",
            title = "Bappa's favourites 👇",
            text = "Get modak, laddu & more delivered at your doorstep!"
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertTrue("Blinkit doorstep delivery marketing promo must be blocked by Commercial Shield", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_COMMERCIAL_PROMO, result.category)
    }

    @Test
    fun testBlinkitGenuineOrderDelivered_GuaranteedSafe() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.grofers.customerapp",
            title = "Blinkit: Order Delivered",
            text = "Your order containing 5 items has been delivered. Enjoy!"
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertFalse("Genuine Blinkit order completed notification must NEVER be blocked", result.isSpam)
    }

    @Test
    fun testCurieFintechBait_DetectedAsFinancialBait() {
        // Real case from user's phone notification history
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.yield.curie_money",
            title = "Your UPI has a side hustle 💼",
            text = "It earns between payments. Use Curie for tonight's dinner."
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("Curie side hustle marketing must be flagged as spam", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_FINANCIAL_BAIT, result.category)
    }

    @Test
    fun testJarDigitalGoldBait_DetectedAsFinancialBait() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.jar.app",
            title = "Gold Price Dropped! 📉",
            text = "Buy 24K Digital Gold in your Apna Gullak today and save in gold."
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("Jar digital gold / gullak bait must be flagged as spam", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_FINANCIAL_BAIT, result.category)
    }

    @Test
    fun testSave8DailySavings_DetectedAsFinancialBait() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.thousandx.save8",
            title = "Daily savings reminder 🪙",
            text = "Save ₹50 today to grow your money with Save8."
        )

        val result = SmartAiClassifier.classify(payload)

        assertTrue("Save8 daily savings promo must be flagged as spam", result.isSpam)
        assertEquals(AiSuggestedRuleEntity.CAT_FINANCIAL_BAIT, result.category)
    }

    @Test
    fun testRapidoRidePromo_BlockedByCommercialShield() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.rapido.passenger",
            title = "50% off on your next Auto ride! 🛺",
            text = "Beat traffic at the lowest price. Book now."
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertTrue("Rapido promotional discount must be blocked", result.isSpam)
    }

    @Test
    fun testRapidoLiveRideUpdate_GuaranteedSafe() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.rapido.passenger",
            title = "Captain assigned!",
            text = "Captain is on the way. Start PIN for your ride is 8192."
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertFalse("Live ride status and start PIN must NEVER be blocked", result.isSpam)
    }

    @Test
    fun testCarInfoInsurancePromo_DetectedAsSpam() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.cuvora.carinfo",
            title = "Car insurance expiring soon!",
            text = "Renew now and get instant discounts on policy."
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertTrue("CarInfo insurance promo must be flagged as spam", result.isSpam)
    }

    @Test
    fun testPaytmHindiCashbackBait_DetectedAsSpam() {
        // Real case from user's phone notification history
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "net.one97.paytm",
            title = "सिर्फ ₹10 ट्रांसफर करें 🚀",
            text = "Paytm UPI से ₹10 भेजें और पाएं ₹30 तक कैशबैक + ₹50 का रिचार्ज और बिल पेमेंट वाउचर 💰"
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertTrue("Paytm Hindi cashback & recharge promo must be flagged as spam", result.isSpam)
    }

    @Test
    fun testPaytmPocketMoneyFeaturePush_DetectedAsSpam() {
        // Real case from user's phone notification history
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "net.one97.paytm",
            title = "Pocket Money Made Simple 👨‍👩‍👧‍👦",
            text = "Give your family money digitally with Paytm—even if they don't have a bank account."
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertTrue("Paytm Pocket Money non-transactional promo push must be flagged as spam", result.isSpam)
    }

    @Test
    fun testPaytmRealUpiPayment_GuaranteedSafe() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "net.one97.paytm",
            title = "Paid ₹150 to Sharma Groceries",
            text = "Money transferred successfully via Paytm UPI. UPI Ref: 1234567890."
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertFalse("Real Paytm UPI payment notifications must NEVER be blocked", result.isSpam)
    }

    @Test
    fun testHindiBankCreditAlert_GuaranteedSafe() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "net.one97.paytm",
            title = "रुपये प्राप्त हुए",
            text = "आपके खाते में ₹500 जमा किए गए। लेन-देन सफल।"
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertFalse("Real Hindi transaction alerts must NEVER be blocked", result.isSpam)
    }

    @Test
    fun testFlipkartCustomVisualAd_DetectedAsSpam() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.flipkart.android",
            title = null,
            text = null,
            channelId = "prodfeedback",
            hasCustomView = true
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertTrue("Flipkart stealth visual ad without text must be flagged as spam", result.isSpam)
        assertEquals("Custom Visual Ad", result.primaryKeyword)
    }

    @Test
    fun testFlipkartOrderDeliveryCustomView_GuaranteedSafe() {
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.flipkart.android",
            title = null,
            text = null,
            channelId = "order",
            channelName = "Communication for your orders",
            hasCustomView = true
        )

        val result = SmartAiClassifier.classify(payload, isCommercialShieldEnabled = true)

        assertFalse("Flipkart transactional order notifications must NEVER be blocked", result.isSpam)
    }
}


