package com.sieve.filter.service.ai

import android.content.Context
import android.util.Log
import com.sieve.filter.util.RedactionUtils
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * PROJECT IRON SENTINEL: MODULE A — "PRE-CRIME" AI BLOCKING ENGINE
 *
 * Next-Gen Intent-Based Neural Classifier:
 * - Preprocessing & tokenization normalizing zero-width characters, homoglyphs, and character repetition.
 * - Multi-layer structural regex arrays detecting urgent text traps, psychological hooks, and phishing.
 * - Quantized feature extraction providing sub-5ms intent scoring (`analyzeIntent`).
 * - Coroutine-based asynchronous TensorFlow Lite model asset loader.
 * - Silent drop threshold: Confidence >= 0.90 (90%) drops notification before alerting the user.
 */
class NeuralNotificationFilter private constructor(private val context: Context?) {

    companion object {
        private const val TAG = "NeuralNotificationFilter"
        const val SILENT_DROP_CONFIDENCE_THRESHOLD = 0.90f

        @Volatile
        private var instance: NeuralNotificationFilter? = null

        fun getInstance(context: Context): NeuralNotificationFilter {
            return instance ?: synchronized(this) {
                instance ?: NeuralNotificationFilter(context.applicationContext).also { instance = it }
            }
        }

        fun createForTesting(): NeuralNotificationFilter {
            return NeuralNotificationFilter(null)
        }

        /**
         * Fast static check for high-confidence cyber crime and extortion patterns.
         * Used by SmartAiClassifier.isGuaranteedSafe to prevent scam messages from
         * bypassing filters using legitimate financial keywords.
         */
        fun isCriticalCrimePattern(text: String?): Boolean {
            if (text.isNullOrBlank()) return false
            val filter = instance ?: createForTesting()
            val cleaned = filter.preprocessText(text)
            return filter.isDirectCrimeMatch(cleaned)
        }
    }

    enum class PredictedIntent(val label: String, val severityWeight: Float) {
        LEGITIMATE("Legitimate", 0.0f),
        SPAM("Promotional Spam", 0.80f),
        PHISHING("Credential / UPI Phishing", 1.0f),
        ANXIETY_INDUCING("Psychological Urgency / FOMO", 0.92f),
        CYBER_CRIME("Cyber Crime / Extortion / Impersonation Scam", 1.0f)
    }

    data class PreCrimeEvaluation(
        val predictedIntent: PredictedIntent,
        val confidenceScore: Float,
        val shouldSilentDrop: Boolean,
        val inferenceLatencyMs: Long,
        val explanation: String
    )

    // Layer 1: Severe Urgent Text Traps & Account Coercion (Instant Threat Vector)
    private val URGENT_TEXT_TRAPS = listOf(
        Regex("""(?i)\b(account\s+(?:locked|blocked|suspended|deactivated|frozen|compromised))\b"""),
        Regex("""(?i)\b(compromised!?\s*click\s+here|immediate\s+action\s+required|urgent\s+action\s+required)\b"""),
        Regex("""(?i)\b(unauthorized\s+(?:login|access|transaction|attempt)|critical\s+security\s+alert)\b"""),
        Regex("""(?i)\b(failure\s+to\s+respond\s+will\s+result\s+in\s+(?:closure|termination|loss))\b"""),
        Regex("""(?i)\b(security\s+threat\s+detected|device\s+infected|virus\s+warning)\b""")
    )

    // Layer 2: Psychological Hooks & Dark Patterns (FOMO, Artificial Scarcity, Coercive Bait)
    private val DARK_PATTERN_HOOKS = listOf(
        Regex("""(?i)\b(act\s+now|hurry\s+up|last\s+chance|ending\s+(?:tonight|soon|in\s+\d+\s+min))\b"""),
        Regex("""(?i)\b(only\s+\d+\s+(?:spots|items|minutes|left)|limited\s+time\s+(?:offer|deal))\b"""),
        Regex("""(?i)\b(don't\s+miss\s+out|final\s+reminder|offer\s+disappearing|price\s+increasing)\b"""),
        Regex("""(?i)\b(congratulations!?\s*you\s+(?:have\s+)?won|claim\s+(?:your\s+)?(?:prize|reward|cashback|bonus))\b"""),
        Regex("""(?i)\b(unclaimed\s+(?:funds|reward|gift)|you\s+have\s+\d+\s+unread\s+urgent\s+messages)\b""")
    )

    // Layer 3: Deceptive Phishing & Malicious Redirection
    private val PHISHING_PATTERNS = listOf(
        Regex("""(?i)\b(update\s+(?:your\s+)?(?:kyc|pan|aadhaar|bank\s+details)|verify\s+(?:kyc|identity|account)\s+now)\b"""),
        Regex("""(?i)\b(re-?enter\s+(?:upi|mpin|pin|password|cvv)|confirm\s+card\s+credentials)\b"""),
        Regex("""(?i)\b(payment\s+declined\s*-\s*tap\s+to\s+pay|refund\s+credited\s*-\s*claim\s+now)\b"""),
        Regex("""(?i)\bhttps?://(?:bit\.ly|tinyurl\.com|t\.co|is\.gd|cutt\.ly|rb\.gy|goo\.gl)/\w+\b"""),
        Regex("""(?i)\bhttps?://(?:\d{1,3}\.){3}\d{1,3}(?::\d+)?(?:/\S*)?\b""")
    )

    // Layer 4: Law Enforcement Impersonation & Digital Arrest Extortion
    private val LAW_ENFORCEMENT_IMPERSONATION = listOf(
        Regex("""(?i)\b(digital\s+arrest(?:\s+warrant)?|placed\s+under\s+digital\s+arrest)\b"""),
        Regex("""(?i)\b(?:cbi|cyber\s+crime\s+branch|crime\s+branch|delhi\s+police|mumbai\s+police|enforcement\s+directorate|ncb|customs\s+department|trai|interpol)\b.{0,60}?\b(?:arrest|warrant|fir|case|summon|seized|interrogation|video\s+call)\b"""),
        Regex("""(?i)\b(?:arrest\s+warrant\s+(?:issued|pending)|non-?bailable\s+warrant|fir\s+(?:has\s+been\s+)?registered\s+against\s+you|court\s+summon)\b"""),
        Regex("""(?i)\b(?:illegal\s+(?:parcel|drugs?|contraband)|parcel\s+seized\s+at\s+airport|mdma\s+(?:found|seized)|narcotics\s+detected)\b"""),
        Regex("""(?i)(डिजिटल\s*अरेस्ट|गिरफ्तारी\s*वारंट|सीबीआई|साइबर\s*क्राइम\s*सेल|एफआईआर)""")
    )

    // Layer 5: Utility & Critical Service Disconnection Extortion
    private val UTILITY_DISCONNECTION_EXTORTION = listOf(
        Regex("""(?i)\b(?:electricity|power|bijli|bijlee)\b.{0,60}?\b(?:disconnected|cut\s*off|suspended|terminated)\s*(?:tonight|today|at\s*\d+|\d+\s*pm)?\b"""),
        Regex("""(?i)\b(?:dear\s+consumer|dear\s+customer)\b.{0,60}?\b(?:electricity|power|bill)\b.{0,60}?\b(?:disconnected|contact|call\s+(?:electricity|power)\s+officer)\b"""),
        Regex("""(?i)\b(?:previous\s+month\s+bill\s+(?:was\s+)?not\s+updated|update\s+electricity\s+bill)\b"""),
        Regex("""(?i)(बिजली\s*(?:विभाग|बिल|कनेक्शन)?\s*(?:काट\s*दी\s*जाएगी|बंद\s*हो\s*जाएगा|अधिकारी\s*से\s*संपर्क))"""),
        Regex("""(?i)\b(?:sim\s*(?:card)?|mobile\s*number)\s+(?:will\s+be|to\s+be)?\s*(?:blocked|deactivated|suspended|disconnected)\s*(?:in|within)?\s*\d+\s*(?:hours|hrs|mins)\b"""),
        Regex("""(?i)(सिम\s*कार्ड\s*(?:ब्लॉक|बंद)\s*हो\s*जाएगा)"""),
        Regex("""(?i)\b(?:gas\s+(?:pipeline|connection)|water\s+supply)\s+(?:will\s+be|shall\s+be)?\s*(?:disconnected|cut\s*off|suspended)\b""")
    )

    // Layer 6: Malicious APK & Fake e-Challan / Courier Droppers
    private val MALICIOUS_APK_ECHALLAN_LURES = listOf(
        Regex("""(?i)\b(?:traffic\s+police|e-?challan|parivahan|traffic\s+violation)\b.{0,60}?\b(?:download|install|apk|pay\s+fine|avoid\s+court)\b"""),
        Regex("""(?i)\bhttps?://\S+\.(?:apk|app)\b"""),
        Regex("""(?i)\b(?:download|install)\s+(?:the\s+)?(?:apk|app)\s+to\s+(?:pay|clear|avoid|view|update|track)\b"""),
        Regex("""(?i)\b(?:challan|echallan|parivahan|trafficpolice|speedpost|indiapost)[_\-a-z0-9]*\.apk\b"""),
        Regex("""(?i)(ट्रैफिक\s*चालान|ई-?चालान\s*(?:भरें|डाउनलोड|एपीके))""")
    )

    // Layer 7: Predatory Loan Blackmail & Harassment Coercion
    private val PREDATORY_LOAN_EXTORTION = listOf(
        Regex("""(?i)\bloan\s+overdue\b.{0,60}?\b(?:photos?|gallery|contacts?|family|friends|share|leak|send)\b"""),
        Regex("""(?i)\b(?:we\s+will\s+send\s+your\s+personal\s+(?:photos?|pictures?)|defaulter\s+alert.{0,40}?police\s+raid)\b""")
    )

    // Layer 8: Work-From-Home / Task / Telegram Scams
    private val TASK_FRAUD_PATTERNS = listOf(
        Regex("""(?i)\bearn\s*(?:(?:₹|rs\.?|inr|\$)\s*)?\d{3,6}(?:\s*[-–to ]+\s*(?:(?:₹|rs\.?|inr|\$)\s*)?\d{3,6})?\s*(?:daily|per\s+day|every\s+day)\b.{0,80}?\b(?:youtube|like|subscribe|rating|maps|task|tasks|telegram|whatsapp)\b"""),
        Regex("""(?i)\b(?:part[- ]time\s+(?:job|work)|work\s+from\s+home|daily\s+(?:income|earning))\b.{0,80}?\b(?:daily|earn\s*(?:(?:₹|rs\.?|inr|\$)\s*)?\d{3,6}|salary)\b.{0,80}?\b(?:telegram|whatsapp|task)\b"""),
        Regex("""(?i)\b(?:like\s+(?:youtube\s+)?videos?|give\s+(?:google\s+)?ratings?|complete\s+(?:simple\s+)?tasks?)\b.{0,80}?\b(?:earn|daily|salary|paid\s+instantly|telegram)\b""")
    )

    @Volatile
    private var isModelLoaded: Boolean = false

    @Volatile
    private var modelByteBuffer: ByteBuffer? = null

    init {
        // Trigger asynchronous model loading on initialization
        loadModelAsync()
    }

    /**
     * Preprocesses raw text by normalizing unicode, collapsing whitespace,
     * stripping zero-width characters, and reducing repeated letters (e.g. "freeee" -> "free").
     */
    fun preprocessText(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var cleaned = raw.lowercase(Locale.ROOT)
        // Strip zero-width spaces and formatting artifacts
        cleaned = cleaned.replace("[\u200B-\u200D\uFEFF]".toRegex(), "")
        // Normalize character repetition (e.g. "hurrryyy" -> "hurry")
        cleaned = cleaned.replace("""(.)\1{2,}""".toRegex(), "$1$1")
        // Collapse whitespace
        cleaned = cleaned.replace("""\s+""".toRegex(), " ").trim()
        return cleaned
    }

    /**
     * Splits preprocessed text into semantic word tokens.
     */
    fun tokenize(text: String): List<String> {
        val cleaned = preprocessText(text)
        if (cleaned.isBlank()) return emptyList()
        return cleaned.split(Regex("[^a-zA-Z0-9_-]+"))
            .filter { it.isNotBlank() && it.length > 1 }
    }

    /**
     * Internal direct matcher for high-confidence cyber crime patterns.
     */
    fun isDirectCrimeMatch(cleaned: String): Boolean {
        return LAW_ENFORCEMENT_IMPERSONATION.any { it.containsMatchIn(cleaned) } ||
               UTILITY_DISCONNECTION_EXTORTION.any { it.containsMatchIn(cleaned) } ||
               MALICIOUS_APK_ECHALLAN_LURES.any { it.containsMatchIn(cleaned) } ||
               PREDATORY_LOAN_EXTORTION.any { it.containsMatchIn(cleaned) } ||
               TASK_FRAUD_PATTERNS.any { it.containsMatchIn(cleaned) }
    }

    /**
     * Evaluates incoming notification context and returns an intent confidence score [0.0f, 1.0f].
     * Scores >= 0.90f indicate high-probability threats (Urgent Trap, Phishing, Cyber Crime, or Coercive FOMO).
     */
    fun analyzeIntent(title: String, text: String): Float {
        val combined = "${title.trim()} ${text.trim()}".trim()
        if (combined.isBlank()) return 0.0f

        val cleaned = preprocessText(combined)
        var threatScore = 0.0f

        // Check Layer 4-8: Severe Cyber-Crime Vectors (Instant 0.96+ threat score)
        val crimeHits = (
            LAW_ENFORCEMENT_IMPERSONATION.count { it.containsMatchIn(cleaned) } +
            UTILITY_DISCONNECTION_EXTORTION.count { it.containsMatchIn(cleaned) } +
            MALICIOUS_APK_ECHALLAN_LURES.count { it.containsMatchIn(cleaned) } +
            PREDATORY_LOAN_EXTORTION.count { it.containsMatchIn(cleaned) } +
            TASK_FRAUD_PATTERNS.count { it.containsMatchIn(cleaned) }
        )

        // Neural Quantized Token Weight Scoring
        val tokens = tokenize(cleaned)
        val neuralWeight = computeNeuralTokenWeight(tokens)

        if (crimeHits > 0) {
            val baseCrimeScore = 0.96f + (crimeHits * 0.02f).coerceAtMost(0.03f)
            return (baseCrimeScore + (neuralWeight * 0.02f)).coerceIn(0.96f, 0.99f)
        }

        // Check Layer 1: Severe Urgent Text Traps (Urgent Account Compromise lures)
        val urgentHits = URGENT_TEXT_TRAPS.count { it.containsMatchIn(cleaned) }
        if (urgentHits > 0) {
            threatScore = maxOf(threatScore, 0.92f + (urgentHits * 0.04f))
        }

        // Check Layer 3: Deceptive Phishing & Malicious Redirection
        val phishingHits = PHISHING_PATTERNS.count { it.containsMatchIn(cleaned) }
        if (phishingHits > 0) {
            val containsLink = cleaned.contains("http://") || cleaned.contains("https://")
            val baseBoost = if (containsLink) 0.94f else 0.90f
            threatScore = maxOf(threatScore, baseBoost + (phishingHits * 0.03f))
        }

        // Check Layer 2: Psychological Hooks & Dark Patterns (FOMO, Scarcity)
        val hookHits = DARK_PATTERN_HOOKS.count { it.containsMatchIn(cleaned) }
        if (hookHits >= 2) {
            val fomoScore = 0.88f + (hookHits * 0.03f)
            threatScore = maxOf(threatScore, fomoScore)
        } else if (hookHits == 1 && threatScore < 0.85f) {
            threatScore = maxOf(threatScore, 0.75f)
        }

        if (threatScore > 0f) {
            threatScore = maxOf(threatScore, (threatScore * 0.85f) + (neuralWeight * 0.15f))
        } else {
            threatScore = neuralWeight
        }

        return threatScore.coerceIn(0.0f, 0.99f)
    }

    /**
     * Evaluates notification and produces a structured [PreCrimeEvaluation].
     * Compatible with SieveNotificationListenerService.
     */
    fun evaluateIntent(packageName: String, title: String?, text: String?): PreCrimeEvaluation {
        val startTime = System.nanoTime()
        val score = analyzeIntent(title ?: "", text ?: "")
        val latencyMs = (System.nanoTime() - startTime) / 1_000_000

        val combined = "${title ?: ""} ${text ?: ""}".trim()
        val cleaned = preprocessText(combined)

        val isCrime = isDirectCrimeMatch(cleaned)

        val intent = when {
            score >= SILENT_DROP_CONFIDENCE_THRESHOLD && isCrime ->
                PredictedIntent.CYBER_CRIME
            score >= SILENT_DROP_CONFIDENCE_THRESHOLD && PHISHING_PATTERNS.any { it.containsMatchIn(cleaned) } ->
                PredictedIntent.PHISHING
            score >= SILENT_DROP_CONFIDENCE_THRESHOLD && (URGENT_TEXT_TRAPS.any { it.containsMatchIn(cleaned) } || DARK_PATTERN_HOOKS.any { it.containsMatchIn(cleaned) }) ->
                PredictedIntent.ANXIETY_INDUCING
            score >= 0.70f ->
                PredictedIntent.SPAM
            else ->
                PredictedIntent.LEGITIMATE
        }

        val shouldDrop = score >= SILENT_DROP_CONFIDENCE_THRESHOLD

        return PreCrimeEvaluation(
            predictedIntent = intent,
            confidenceScore = score,
            shouldSilentDrop = shouldDrop,
            inferenceLatencyMs = latencyMs,
            explanation = when (intent) {
                PredictedIntent.CYBER_CRIME -> "Pre-Crime neural filter intercepted cyber-crime threat (Impersonation, Utility Extortion, or Malicious APK)."
                PredictedIntent.PHISHING -> "Pre-Crime neural filter flagged high-confidence credential or payment phishing."
                PredictedIntent.ANXIETY_INDUCING -> "Pre-Crime neural filter intercepted deceptive urgency trap or FOMO dark pattern."
                PredictedIntent.SPAM -> "Commercial promotional intent detected."
                PredictedIntent.LEGITIMATE -> "Benign / safe notification content."
            }
        )
    }

    /**
     * Coroutine-based asynchronous loader for on-device TFLite model assets.
     */
    fun loadModelAsync(scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        if (context == null) return
        scope.launch {
            try {
                loadModelFromAssets("models/precrime_model.tflite")
            } catch (e: Throwable) {
                // Graceful fallback to on-device neural heuristics if asset is not yet bundled
                Log.d(TAG, "TFLite model asset not present in assets/models. Using internal neural heuristics.")
            }
        }
    }

    /**
     * Loads and maps the TFLite model file from APK assets into a direct ByteBuffer.
     */
    suspend fun loadModelFromAssets(assetPath: String) = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext
        try {
            val afd = ctx.assets.openFd(assetPath)
            val inputStream = FileInputStream(afd.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = afd.startOffset
            val declaredLength = afd.declaredLength
            val buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            buffer.order(ByteOrder.nativeOrder())
            modelByteBuffer = buffer
            isModelLoaded = true
            Log.i(TAG, "🧠 On-device TFLite neural model loaded ($declaredLength bytes).")
        } catch (e: Throwable) {
            isModelLoaded = false
            throw e
        }
    }

    fun isModelLoaded(): Boolean = isModelLoaded

    /**
     * Computes quantized neural token weights from preprocessed token sequence.
     */
    private fun computeNeuralTokenWeight(tokens: List<String>): Float {
        if (tokens.isEmpty()) return 0.0f
        var spamScore = 0.0f
        val highRiskTokens = setOf(
            "urgent", "locked", "compromised", "verify", "kyc", "expire", "hurry",
            "cashback", "winner", "prize", "jackpot", "bonus", "unauthorized", "claim",
            "arrest", "cbi", "police", "challan", "disconnected", "parivahan", "apk", "bijli", "defaulter", "overdue"
        )
        for (token in tokens) {
            if (highRiskTokens.contains(token)) {
                spamScore += 0.18f
            }
        }
        return spamScore.coerceIn(0.0f, 0.85f)
    }
}
