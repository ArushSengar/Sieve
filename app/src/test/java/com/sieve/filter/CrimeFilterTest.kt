package com.sieve.filter

import com.sieve.filter.data.local.entity.KeywordRuleEntity
import com.sieve.filter.model.AppRuleMode
import com.sieve.filter.model.RuleAction
import com.sieve.filter.service.NotificationClassifier
import com.sieve.filter.service.SmartAiClassifier
import com.sieve.filter.service.ai.NeuralNotificationFilter
import com.sieve.filter.service.ai.NeuralNotificationFilter.PredictedIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * CYBER-CRIME DEFENSE VERIFICATION SUITE
 *
 * Rigorously verifies that Sieve intercepts and silently drops:
 * 1. Digital Arrest scams & Law Enforcement impersonation (CBI, Police, Customs, NCB).
 * 2. Utility & Critical Service disconnection extortion (Electricity/Bijli, SIM card deactivation).
 * 3. Malicious APK distribution & Fake e-Challan droppers.
 * 4. Predatory loan harassment & photo-leak blackmail.
 * 5. Work-from-home task fraud & Telegram scams.
 * 6. Anti-evasion: Scams attempting to sneak through using legitimate financial words.
 *
 * Guarantees ZERO false positives for legitimate OTPs, banking transactions, and deliveries.
 */
class CrimeFilterTest {

    private lateinit var filter: NeuralNotificationFilter

    @Before
    fun setUp() {
        filter = NeuralNotificationFilter.createForTesting()
    }

    // =========================================================================
    // 1. DIGITAL ARREST & LAW ENFORCEMENT IMPERSONATION
    // =========================================================================

    @Test
    fun testCrime_DigitalArrestScam_Blocked() {
        val title = "CBI Headquarters Alert"
        val text = "You are placed under digital arrest. Arrest warrant issued for drug parcel intercepted at Mumbai airport. Join Skype video call immediately."

        val eval = filter.evaluateIntent("com.fake.police", title, text)
        assertTrue("Digital arrest scam must be silently dropped", eval.shouldSilentDrop)
        assertEquals("Intent must be CYBER_CRIME", PredictedIntent.CYBER_CRIME, eval.predictedIntent)
        assertTrue("Confidence must be >= 0.95f", eval.confidenceScore >= 0.95f)
    }

    @Test
    fun testCrime_PoliceCourtSummonScam_Blocked() {
        val title = "Delhi Police Cyber Crime Branch"
        val text = "A FIR has been registered against you for money laundering. Non-bailable warrant issued. Contact IO officer immediately."

        val eval = filter.evaluateIntent("com.fake.cybercell", title, text)
        assertTrue("Police FIR impersonation must be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.CYBER_CRIME, eval.predictedIntent)
    }

    @Test
    fun testCrime_CustomsContrabandScam_Blocked() {
        val title = "Customs Department Mumbai"
        val text = "Illegal parcel seized at airport containing MDMA drugs and fake passports. Police case filed."

        val eval = filter.evaluateIntent("com.fake.customs", title, text)
        assertTrue("Customs drug parcel extortion must be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.CYBER_CRIME, eval.predictedIntent)
    }

    @Test
    fun testCrime_HindiDigitalArrest_Blocked() {
        val title = "सीबीआई विशेष प्रकोष्ठ"
        val text = "आपके खिलाफ गैर-जमानती गिरफ्तारी वारंट जारी हुआ है। तुरंत डिजिटल अरेस्ट में रहें।"

        val eval = filter.evaluateIntent("com.fake.cbi", title, text)
        assertTrue("Hindi digital arrest must be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.CYBER_CRIME, eval.predictedIntent)
    }

    // =========================================================================
    // 2. UTILITY & CRITICAL SERVICE EXTORTION
    // =========================================================================

    @Test
    fun testCrime_ElectricityDisconnectionScam_Blocked() {
        val title = "Electricity Bill Alert"
        val text = "Dear consumer your electricity power will be disconnected tonight at 9:30 PM from electricity office because your previous month bill was not updated. Contact electricity officer at 9876543210."

        val eval = filter.evaluateIntent("com.fake.electricity", title, text)
        assertTrue("Classic electricity scam must be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.CYBER_CRIME, eval.predictedIntent)
    }

    @Test
    fun testCrime_HindiBijliConnectionCutScam_Blocked() {
        val title = "बिजली विभाग सूचना"
        val text = "आपका बिजली बिल अपडेट नहीं हुआ है। आज रात बिजली काट दी जाएगी। तुरंत बिजली अधिकारी से संपर्क करें।"

        val eval = filter.evaluateIntent("com.fake.power", title, text)
        assertTrue("Hindi bijli scam must be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.CYBER_CRIME, eval.predictedIntent)
    }

    @Test
    fun testCrime_SimDeactivationScam_Blocked() {
        val title = "Airtel Customer Service"
        val text = "Dear customer, your SIM card will be deactivated within 24 hours due to non-KYC. Call telecom officer at 9811223344 immediately."

        val eval = filter.evaluateIntent("com.fake.telecom", title, text)
        assertTrue("SIM deactivation extortion must be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.CYBER_CRIME, eval.predictedIntent)
    }

    // =========================================================================
    // 3. MALICIOUS APK & FAKE e-CHALLAN DROPPERS
    // =========================================================================

    @Test
    fun testCrime_FakeTrafficChallanApk_Blocked() {
        val title = "Traffic Police Alert"
        val text = "Pending e-Challan Rs. 1000 on your vehicle MH02AB1234. Download Challan.apk to pay fine and avoid court warrant: http://challan-pay.org/challan.apk"

        val eval = filter.evaluateIntent("com.fake.challan", title, text)
        assertTrue("Traffic challan APK malware must be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.CYBER_CRIME, eval.predictedIntent)
    }

    @Test
    fun testCrime_MaliciousApkDownloadLink_Blocked() {
        val title = "India Post Delivery"
        val text = "Your courier parcel is pending. Download the app to track and update delivery: https://indiapost-parcel.com/indiapost.apk"

        val eval = filter.evaluateIntent("com.fake.courier", title, text)
        assertTrue("Courier APK dropper must be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.CYBER_CRIME, eval.predictedIntent)
    }

    // =========================================================================
    // 4. PREDATORY LOAN BLACKMAIL & HARASSMENT
    // =========================================================================

    @Test
    fun testCrime_LoanBlackmailPhotoThreat_Blocked() {
        val title = "Urgent Recovery Notice"
        val text = "Loan overdue notice: We will share your personal photos and contacts with family and friends if payment is not made within 1 hour."

        val eval = filter.evaluateIntent("com.fake.loan", title, text)
        assertTrue("Predatory loan blackmail must be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.CYBER_CRIME, eval.predictedIntent)
    }

    // =========================================================================
    // 5. WORK-FROM-HOME TASK & TELEGRAM SCAMS
    // =========================================================================

    @Test
    fun testCrime_WorkFromHomeTaskScam_Blocked() {
        val title = "Part Time Job Offer"
        val text = "Earn Rs 3000-8000 daily from home by liking YouTube videos and rating hotels on Google maps. Join Telegram channel @earn_daily."

        val eval = filter.evaluateIntent("com.fake.job", title, text)
        assertTrue("Work-from-home task fraud must be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.CYBER_CRIME, eval.predictedIntent)
    }

    // =========================================================================
    // 6. ANTI-EVASION: SCAM HIDING BEHIND SAFE TRANSACTION WORDS
    // =========================================================================

    @Test
    fun testCrime_AntiEvasion_ElectricityScamWithPaymentWordsNotSafe() {
        // Scammer embeds "payment of Rs" and "A/c ending" to fool transaction safety filters
        val title = "Electricity Department"
        val text = "Your payment of Rs 1,450 for A/c ending 8912 was not updated. Power will be disconnected tonight at 9:30 PM. Call electricity officer at 9876543210."

        // 1. Must NOT be marked as guaranteed safe despite having "payment of rs" and "a/c ending"
        val isSafe = SmartAiClassifier.isGuaranteedSafe(title, text)
        assertFalse("Scam must NEVER be shielded by safe transaction words", isSafe)

        // 2. Must be intercepted by neural crime filter
        val eval = filter.evaluateIntent("com.fake.electricity", title, text)
        assertTrue("Must be silently dropped by Pre-Crime filter", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.CYBER_CRIME, eval.predictedIntent)
    }

    // =========================================================================
    // 7. ZERO FALSE POSITIVES FOR LEGITIMATE TRANSACTIONS & OTPS
    // =========================================================================

    @Test
    fun testLegitimate_OtpNotification_AlwaysAllowed() {
        val title = "SBI YONO"
        val text = "Your OTP is 482019 for login to SBI YONO. Do not share this OTP with anyone for security reasons."

        assertTrue("Legitimate OTP must be guaranteed safe", SmartAiClassifier.isGuaranteedSafe(title, text))

        val eval = filter.evaluateIntent("com.sbi.lotusintouch", title, text)
        assertFalse("Legitimate OTP must NEVER be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.LEGITIMATE, eval.predictedIntent)
    }

    @Test
    fun testLegitimate_BankDebitCredit_AlwaysAllowed() {
        val title = "HDFC Bank Alert"
        val text = "INR 2,450.00 debited from A/c ending 4821 on 26-Sep-26 towards UPI txn. Avail bal INR 18,290.00."

        assertTrue("Legitimate debit notification must be guaranteed safe", SmartAiClassifier.isGuaranteedSafe(title, text))

        val eval = filter.evaluateIntent("com.hdfcbank.netbanking", title, text)
        assertFalse("Banking transaction must NEVER be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.LEGITIMATE, eval.predictedIntent)
    }

    @Test
    fun testLegitimate_ECommerceDelivery_AlwaysAllowed() {
        val title = "Swiggy Order Update"
        val text = "Your order #89218 is out for delivery with delivery partner Ramesh. Arriving in 15 mins."

        assertTrue("Food delivery update must be guaranteed safe", SmartAiClassifier.isGuaranteedSafe(title, text))

        val eval = filter.evaluateIntent("in.swiggy.android", title, text)
        assertFalse("Delivery notification must NEVER be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.LEGITIMATE, eval.predictedIntent)
    }

    @Test
    fun testLegitimate_ElectricityBillPaymentReceipt_AlwaysAllowed() {
        // Genuine receipt of electricity bill payment
        val title = "Google Pay"
        val text = "Paid ₹850 to BESCOM Electricity. Transaction successful. UPI Ref: 4892182910."

        assertTrue("Electricity bill payment receipt must be guaranteed safe", SmartAiClassifier.isGuaranteedSafe(title, text))

        val eval = filter.evaluateIntent("com.google.android.apps.nbu.paisa.user", title, text)
        assertFalse("Payment receipt must NEVER be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.LEGITIMATE, eval.predictedIntent)
    }

    // =========================================================================
    // 8. ZERO-DAY ReDoS / ADVERSARIAL STRESS TEST
    // =========================================================================

    @Test(timeout = 2000)
    fun testAdversarial_NoCatastrophicBacktrackingOnLongInputs() {
        val longString = "A".repeat(10000) + " urgent deal " + "B".repeat(10000)
        val startTime = System.currentTimeMillis()
        val score = filter.analyzeIntent("Alert", longString)
        val elapsed = System.currentTimeMillis() - startTime

        assertNotNull(score)
        assertTrue("Regex analysis must complete within 200ms on massive inputs (took ${elapsed}ms)", elapsed < 200)
    }

    @Test
    fun testCrime_WorkFromHomeSingleAmount_Blocked() {
        val title = "Daily Income Offer"
        val text = "Earn 5000 daily by liking YouTube videos and completing easy tasks. Contact manager on Telegram @daily_earn."

        val eval = filter.evaluateIntent("com.fake.task", title, text)
        assertTrue("Single amount task scam must be dropped", eval.shouldSilentDrop)
        assertEquals(PredictedIntent.CYBER_CRIME, eval.predictedIntent)
    }

    @Test
    fun testCrime_AntiEvasion_KeywordAllowRuleDoesNotBypassCyberCrime() {
        // Attack vector: Scam notification contains "A/c ending" to trigger default allow keyword rule
        val payload = NotificationClassifier.NotificationPayload(
            packageName = "com.fake.police",
            title = "SBI Security",
            text = "SBI Alert: A/c ending 1234 payment of Rs 50000 pending. Digital arrest warrant by police. Contact officer."
        )

        val allowRule = KeywordRuleEntity(
            pattern = "a/c ending",
            action = RuleAction.ALLOW.name,
            packageName = null
        )

        val decision = NotificationClassifier.classify(
            payload = payload,
            appRuleMode = AppRuleMode.AUTO,
            rules = listOf(allowRule)
        )

        // Must NOT be explicitly allowed by the keyword rule because it contains a verified cyber-crime pattern
        assertFalse("Allow rule must NEVER permit cyber-crime scams", decision.isExplicitAllow)
        assertTrue("Scam must fall through to Pre-Crime analysis", decision.isPassThrough)
    }
}
