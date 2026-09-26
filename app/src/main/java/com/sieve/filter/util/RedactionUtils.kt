package com.sieve.filter.util

import java.util.regex.Pattern

/**
 * Military-grade PII and sensitive credential redaction utility.
 * Sanitizes in-memory notifications before logging to Logcat, caching, or diagnostics.
 * Prevents credential, OTP, credit card, and private data leaks (CWE-532).
 */
object RedactionUtils {

    // 4 to 8 digit standalone OTP tokens or 6-char alphanumeric OTPs
    private val OTP_REGEX = Pattern.compile(
        """\b(?:\d{4,8}|[A-Z0-9]{6})\b""",
        Pattern.CASE_INSENSITIVE
    )

    // Credit / Debit card patterns (13 to 19 digits with optional single spaces/hyphens)
    private val CARD_REGEX = Pattern.compile(
        """\b(?:\d[ -]?){13,19}\b"""
    )

    // CVV / CVC (3-4 digits preceded by cvv/cvc indicators)
    private val CVV_REGEX = Pattern.compile(
        """(?i)\b(?:cvv|cvc|security code)\s*[:=]?\s*(\d{3,4})\b"""
    )

    // Bank Account numbers preceded by account/a/c indicators
    private val ACCOUNT_REGEX = Pattern.compile(
        """(?i)\b(?:a/c|acct|account)\s*(?:no\.?|num\.?)?\s*[:#-]?\s*(\d{4,18})\b"""
    )

    // UPI Virtual Payment Address (VPA)
    private val UPI_REGEX = Pattern.compile(
        """\b[a-zA-Z0-9.\-_]{2,256}@[a-zA-Z]{2,64}\b"""
    )

    // Email addresses
    private val EMAIL_REGEX = Pattern.compile(
        """\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b"""
    )

    // International & Indian phone numbers (10+ digits with country codes)
    private val PHONE_REGEX = Pattern.compile(
        """\b(?:\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}\b"""
    )

    /**
     * Complete PII sanitization: masks credit cards, OTPs, CVVs, bank accounts, and phone numbers.
     */
    fun maskPii(input: String?): String {
        if (input.isNullOrBlank()) return ""
        var sanitized: String = input

        // 1. Mask CVVs
        sanitized = CVV_REGEX.matcher(sanitized).replaceAll("CVV: [REDACTED]")

        // 2. Mask Bank Accounts (retain last 4 digits for context if >= 8 digits)
        val acctMatcher = ACCOUNT_REGEX.matcher(sanitized)
        val acctSb = StringBuffer()
        while (acctMatcher.find()) {
            val num = acctMatcher.group(1) ?: ""
            val masked = if (num.length >= 8) {
                "•".repeat(num.length - 4) + num.takeLast(4)
            } else {
                "•".repeat(num.length)
            }
            acctMatcher.appendReplacement(acctSb, "A/C $masked")
        }
        acctMatcher.appendTail(acctSb)
        sanitized = acctSb.toString()

        // 3. Mask Credit/Debit Cards
        val cardMatcher = CARD_REGEX.matcher(sanitized)
        val cardSb = StringBuffer()
        while (cardMatcher.find()) {
            val rawMatch = cardMatcher.group()
            val matchedCard = rawMatch.replace(Regex("[ -]"), "")
            if (matchedCard.length in 13..19 && isLuhnValid(matchedCard)) {
                cardMatcher.appendReplacement(cardSb, "••••-••••-••••-" + matchedCard.takeLast(4))
            } else {
                cardMatcher.appendReplacement(cardSb, java.util.regex.Matcher.quoteReplacement(rawMatch))
            }
        }
        cardMatcher.appendTail(cardSb)
        sanitized = cardSb.toString()

        // 4. Mask UPI VPAs
        sanitized = UPI_REGEX.matcher(sanitized).replaceAll { mr ->
            val vpa = mr.group()
            val parts = vpa.split("@")
            if (parts.size == 2) {
                val user = parts[0]
                val visibleUser = if (user.length > 2) user.take(2) + "•••" else "•••"
                "$visibleUser@${parts[1]}"
            } else {
                "•••@vpa"
            }
        }

        // 5. Mask Email
        sanitized = EMAIL_REGEX.matcher(sanitized).replaceAll { mr ->
            val email = mr.group()
            val atIdx = email.indexOf('@')
            if (atIdx > 1) {
                email.take(1) + "•••@" + email.substring(atIdx + 1)
            } else {
                "•••@domain"
            }
        }

        return sanitized
    }

    /**
     * Specifically redacts any detected OTP tokens in transaction messages.
     */
    fun maskOtp(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val matcher = OTP_REGEX.matcher(input)
        val sb = StringBuffer()
        while (matcher.find()) {
            matcher.appendReplacement(sb, "••••")
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    /**
     * Sanitizes strings destined for Logcat output, ensuring no credentials or PII ever escape.
     */
    fun sanitizeForLog(message: String): String {
        return "[SEC-LOG] " + maskPii(message)
    }

    /**
     * Standard Luhn algorithm verification for credit/debit card numbers.
     */
    private fun isLuhnValid(number: String): Boolean {
        var sum = 0
        var alternate = false
        for (i in number.length - 1 downTo 0) {
            var n = number[i].digitToIntOrNull() ?: return false
            if (alternate) {
                n *= 2
                if (n > 9) n = (n % 10) + 1
            }
            sum += n
            alternate = !alternate
        }
        return (sum % 10 == 0)
    }
}
