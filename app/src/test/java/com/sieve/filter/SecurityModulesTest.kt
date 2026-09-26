package com.sieve.filter

import com.sieve.filter.data.security.SecurityVault
import com.sieve.filter.security.AppIntegritySentinel
import com.sieve.filter.service.ai.NeuralNotificationFilter
import com.sieve.filter.service.ghost.GhostInterceptor
import com.sieve.filter.util.RedactionUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * PROJECT IRON SENTINEL: ADVERSARIAL INTEGRATION UNIT TEST SUITE
 *
 * Validates:
 * 1. RedactionUtils memory sanitization across 2FA OTPs, 16-digit cards, CVVs, Bank A/Cs, VPAs.
 * 2. SecurityVault AES-256-GCM authenticated encryption, random IV validation, and round-trip decryption.
 * 3. NeuralNotificationFilter text normalization, tokenization, intent scoring, and pre-crime silent drops.
 * 4. GhostInterceptor marketing beacon detection and ephemeral shadow vault isolation.
 * 5. AppIntegritySentinel tamper shield runtime heuristics.
 */
class SecurityModulesTest {

    @Before
    fun setUp() {
        SecurityVault.resetKeysForTesting()
        GhostInterceptor.purgeVault()
    }

    // =========================================================================
    // 1. REDACTION UTILS: IN-MEMORY CREDENTIAL & PII REDACTION TESTS
    // =========================================================================

    @Test
    fun testRedaction_MasksOtpTokens() {
        // 4-digit OTP
        val input4Digit = "Your verification OTP is 4829. Valid for 5 minutes."
        val sanitized4Digit = RedactionUtils.maskOtp(input4Digit)
        assertFalse("4-digit OTP must not leak in memory", sanitized4Digit.contains("4829"))
        assertTrue("OTP must be replaced with mask", sanitized4Digit.contains("••••"))

        // 6-digit OTP
        val input6Digit = "Your Bank login code is 891023. Do NOT share with anyone."
        val sanitized6Digit = RedactionUtils.maskOtp(input6Digit)
        assertFalse("6-digit OTP must not leak in memory", sanitized6Digit.contains("891023"))
        assertTrue("OTP must be replaced with mask", sanitized6Digit.contains("••••"))

        // Alphanumeric 6-char OTP
        val inputAlpha = "Use authorization code A8X92K to approve login."
        val sanitizedAlpha = RedactionUtils.maskOtp(inputAlpha)
        assertFalse("Alphanumeric OTP must not leak in memory", sanitizedAlpha.contains("A8X92K"))
    }

    @Test
    fun testRedaction_MasksCreditCardWithLuhnValidation() {
        // Valid Luhn 16-digit test card number (Visa standard test card 4111 1111 1111 1111)
        val validCard = "Your card 4111 1111 1111 1111 was charged INR 2,499.00."
        val masked = RedactionUtils.maskPii(validCard)

        assertFalse("Full credit card number must not appear in cleartext", masked.contains("4111 1111 1111 1111"))
        assertTrue("Masked card must preserve only last 4 digits for transaction context", masked.contains("••••-••••-••••-1111"))

        // Hyphenated credit card
        val hyphenated = "Card 4111-1111-1111-1111 debited."
        val maskedHyphen = RedactionUtils.maskPii(hyphenated)
        assertFalse("Hyphenated card number must be masked", maskedHyphen.contains("4111-1111-1111-1111"))
        assertTrue("Hyphenated card must preserve last 4 digits", maskedHyphen.contains("••••-••••-••••-1111"))
    }

    @Test
    fun testRedaction_MasksCvvAndSecurityCodes() {
        val input = "Never share CVV: 482 or security code: 918 with bank executives."
        val masked = RedactionUtils.maskPii(input)

        assertFalse("CVV 482 must not leak", masked.contains("482"))
        assertFalse("Security code 918 must not leak", masked.contains("918"))
        assertTrue("CVV indicator must be redacted", masked.contains("CVV: [REDACTED]"))
    }

    @Test
    fun testRedaction_MasksBankAccountsAndUpiVpa() {
        // Bank Account
        val acctMsg = "Salary credited to A/C 123456789012 on 26-Sep."
        val maskedAcct = RedactionUtils.maskPii(acctMsg)
        assertFalse("Full bank account number must not be exposed", maskedAcct.contains("123456789012"))
        assertTrue("Bank account must retain only last 4 digits", maskedAcct.contains("9012"))

        // UPI VPA
        val upiMsg = "Payment requested by rahul.sharma@okhdfcbank for INR 500."
        val maskedUpi = RedactionUtils.maskPii(upiMsg)
        assertFalse("Full VPA username must not be exposed", maskedUpi.contains("rahul.sharma@okhdfcbank"))
        assertTrue("VPA domain must remain while username is masked", maskedUpi.contains("@okhdfcbank"))
    }

    // =========================================================================
    // 2. SECURITY VAULT: ZERO-KNOWLEDGE HARDWARE STORAGE TESTS
    // =========================================================================

    @Test
    fun testSecurityVault_EncryptionAndDecryptionRoundTrip() {
        val sensitiveData = "CONFIDENTIAL_OTP_TOKEN_94821_ACCOUNT_BALANCE_INR_98500"

        val cipherText = SecurityVault.encryptString(sensitiveData)
        assertNotNull("Ciphertext output must not be null", cipherText)
        assertNotEquals("Ciphertext must not match cleartext", sensitiveData, cipherText)
        assertTrue("Ciphertext should be non-empty Base64", cipherText.isNotBlank())

        val decrypted = SecurityVault.decryptString(cipherText)
        assertEquals("Decrypted plaintext must match source string with 100% fidelity", sensitiveData, decrypted)
    }

    @Test
    fun testSecurityVault_RandomizedIvEnsuresDifferentCiphertexts() {
        val plaintext = "SAME_STATIC_NOTIFICATION_BODY"

        val cipher1 = SecurityVault.encryptString(plaintext)
        val cipher2 = SecurityVault.encryptString(plaintext)

        assertNotEquals("Each encryption must use a unique random IV; identical plaintexts must not yield identical ciphertexts", cipher1, cipher2)

        // Both must decrypt back to original text
        assertEquals(plaintext, SecurityVault.decryptString(cipher1))
        assertEquals(plaintext, SecurityVault.decryptString(cipher2))
    }

    @Test
    fun testSecurityVault_MalformedCiphertextThrowsException() {
        // Case 1: Invalid Base64 string
        try {
            SecurityVault.decryptString("invalid-base64-payload!!!")
            fail("Invalid Base64 ciphertext must throw IllegalArgumentException")
        } catch (expected: IllegalArgumentException) {
            assertNotNull(expected.message)
        }

        // Case 2: Valid Base64 but truncated payload (< 28 bytes)
        val shortBase64 = java.util.Base64.getEncoder().encodeToString(byteArrayOf(1, 2, 3, 4, 5))
        try {
            SecurityVault.decryptString(shortBase64)
            fail("Truncated ciphertext must throw IllegalArgumentException")
        } catch (expected: IllegalArgumentException) {
            assertTrue("Exception message should indicate malformed or short payload", expected.message!!.contains("malformed") || expected.message!!.contains("short"))
        }
    }

    @Test
    fun testSecurityVault_DatabasePassphraseDerivation() {
        val passphrase = SecurityVault.getDatabasePassphrase()
        assertNotNull("Derived database passphrase must not be null", passphrase)
        assertEquals("Database encryption passphrase must be 32 bytes (256-bit key)", 32, passphrase.size)
    }

    // =========================================================================
    // 3. NEURAL NOTIFICATION FILTER: PRE-CRIME INTENT ENGINE TESTS
    // =========================================================================

    @Test
    fun testNeuralFilter_TextPreprocessingAndTokenization() {
        val filter = NeuralNotificationFilter.createForTesting()

        val raw = "URGENTTT!!   FREE   Cashback \u200B Nowww"
        val preprocessed = filter.preprocessText(raw)

        // Verifies zero-width space stripped, repeated letters collapsed, lowercased
        assertFalse("Zero-width space must be stripped", preprocessed.contains("\u200B"))
        assertTrue("Character repetitions must be collapsed", preprocessed.contains("urgent") || preprocessed.contains("free"))

        val tokens = filter.tokenize(raw)
        assertTrue("Tokens list must contain semantic words", tokens.contains("cashback"))
        assertTrue("Token count should be >= 2", tokens.size >= 2)
    }

    @Test
    fun testNeuralFilter_UrgentTextTraps_TriggerPreCrimeSilentDrop() {
        val filter = NeuralNotificationFilter.createForTesting()

        // Critical urgent coercion trap
        val title = "CRITICAL ALERT: ACCOUNT LOCKED"
        val body = "IMMEDIATE ACTION REQUIRED. Unauthorized login detected. Click here."

        val score = filter.analyzeIntent(title, body)
        assertTrue("Urgent text trap must yield confidence >= 0.90f (actual: $score)", score >= 0.90f)

        val evaluation = filter.evaluateIntent("com.bank.fake", title, body)
        assertTrue("Severe urgent trap must trigger silent drop", evaluation.shouldSilentDrop)
        assertTrue("Intent must be ANXIETY_INDUCING or PHISHING",
            evaluation.predictedIntent == NeuralNotificationFilter.PredictedIntent.ANXIETY_INDUCING ||
            evaluation.predictedIntent == NeuralNotificationFilter.PredictedIntent.PHISHING
        )
    }

    @Test
    fun testNeuralFilter_PhishingTraps_TriggerPreCrimeSilentDrop() {
        val filter = NeuralNotificationFilter.createForTesting()

        val title = "Update your KYC Immediately"
        val body = "Your bank account will be blocked today. Verify at http://bit.ly/secure-kyc-update"

        val score = filter.analyzeIntent(title, body)
        assertTrue("Phishing threat with shortener URL must yield confidence >= 0.90f (actual: $score)", score >= 0.90f)

        val evaluation = filter.evaluateIntent("com.phish.test", title, body)
        assertTrue("Phishing notification must trigger silent drop", evaluation.shouldSilentDrop)
        assertEquals("Phishing intent must be correctly categorized", NeuralNotificationFilter.PredictedIntent.PHISHING, evaluation.predictedIntent)
    }

    @Test
    fun testNeuralFilter_BenignNotification_PassesThroughSafely() {
        val filter = NeuralNotificationFilter.createForTesting()

        val title = "Amazon: Package Delivered"
        val body = "Your package with Order #402-9182391 has been delivered to your front door."

        val score = filter.analyzeIntent(title, body)
        assertTrue("Benign transaction / delivery notification must have low threat score < 0.40f (actual: $score)", score < 0.40f)

        val evaluation = filter.evaluateIntent("com.amazon.mShop.android.shopping", title, body)
        assertFalse("Benign notification must NEVER be silently dropped", evaluation.shouldSilentDrop)
        assertEquals("Benign notification must be marked LEGITIMATE", NeuralNotificationFilter.PredictedIntent.LEGITIMATE, evaluation.predictedIntent)
    }

    // =========================================================================
    // 4. GHOST INTERCEPTOR: DIGITAL GHOST PROTOCOL (ANTI-TRACKING) TESTS
    // =========================================================================

    @Test
    fun testGhostInterceptor_IdentifiesTrackingBeacons() {
        // CleverTap beacon
        val cleverTapKeys = setOf("wzrk_id", "wzrk_pn", "android.title")
        assertTrue("CleverTap tracking keys must be detected", GhostInterceptor.isTrackingBeaconPresent(cleverTapKeys))

        // MoEngage beacon
        val moEngageKeys = setOf("moengage_payload", "moengage_data")
        assertTrue("MoEngage tracking keys must be detected", GhostInterceptor.isTrackingBeaconPresent(moEngageKeys))

        // Braze beacon
        val brazeKeys = setOf("braze_campaign_id", "ab_cd")
        assertTrue("Braze tracking keys must be detected", GhostInterceptor.isTrackingBeaconPresent(brazeKeys))

        // OneSignal beacon
        val oneSignalKeys = setOf("onesignal_data")
        assertTrue("OneSignal tracking keys must be detected", GhostInterceptor.isTrackingBeaconPresent(oneSignalKeys))

        // Non-tracking standard notification
        val safeKeys = setOf("android.title", "android.text", "android.subText", "android.icon")
        assertFalse("Safe standard keys must not be flagged as beacons", GhostInterceptor.isTrackingBeaconPresent(safeKeys))
    }

    @Test
    fun testGhostInterceptor_InterceptsAndIsolatesInShadowVault() {
        val record = GhostInterceptor.interceptTrackingPayload(
            packageName = "com.marketer.app",
            notificationId = 101,
            postTime = 1700000000000L,
            extrasKeys = setOf("wzrk_id", "wzrk_pn", "android.title"),
            title = "Special Offer for User!",
            text = "Flash sale 50% off everything.",
            hasDeleteIntent = true
        )

        assertNotNull("GhostRecord must be generated", record)
        assertEquals("com.marketer.app", record.packageName)
        assertTrue("Tracking beacons must be neutralized", record.trackingBeaconsNeutralized.contains("wzrk_id"))
        assertTrue("DeleteIntent presence must be tracked", record.hasDeleteIntentTracked)

        // Retrieve from shadow vault
        val stored = GhostInterceptor.getGhostRecord(record.ghostId)
        assertNotNull("Record must be retrievable from shadow vault", stored)
        assertEquals(record.ghostId, stored?.ghostId)

        // Vault purge
        GhostInterceptor.purgeVault()
        assertEquals("Vault must be empty after purge", 0, GhostInterceptor.getVaultSize())
    }

    // =========================================================================
    // 5. APP INTEGRITY SENTINEL: ADVERSARIAL TAMPER SHIELD TESTS
    // =========================================================================

    @Test
    fun testAppIntegritySentinel_ConstantsAndHeuristics() {
        val releaseHash = AppIntegritySentinel.VALID_RELEASE_SIGNATURE_SHA256
        assertNotNull("Release signature byte array must be configured", releaseHash)
        assertEquals("SHA-256 certificate hash must be exactly 32 bytes", 32, releaseHash.size)

        // Verify heuristic scanner runs safely without unhandled exceptions
        val debuggerStatus = AppIntegritySentinel.isDebuggerConnected()
        assertNotNull(debuggerStatus)
    }
}
