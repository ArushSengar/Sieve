package com.sieve.filter.service.dictionaries

import java.io.InputStream
import java.util.regex.Pattern

data class SpamTriggerItem(
    val trigger: String,
    val keyword: String,
    val category: String
)

data class LanguageDictionary(
    val version: Int,
    val language: String,
    val code: String,
    val guaranteedSafe: List<String>,
    val spamTriggers: List<SpamTriggerItem>
)

object DictionaryParser {

    fun parse(inputStream: InputStream): LanguageDictionary {
        val content = inputStream.bufferedReader().use { it.readText() }

        val version = extractInt(content, "version") ?: 1
        val language = extractString(content, "language") ?: "unknown"
        val code = extractString(content, "code") ?: "xx"

        // Extract guaranteed_safe array
        val safeSectionRegex = Pattern.compile("\"guaranteed_safe\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL)
        val safeMatcher = safeSectionRegex.matcher(content)
        val guaranteedSafe = mutableListOf<String>()

        if (safeMatcher.find()) {
            val safeBlock = safeMatcher.group(1) ?: ""
            val stringMatcher = Pattern.compile("\"([^\"]+)\"").matcher(safeBlock)
            while (stringMatcher.find()) {
                val item = stringMatcher.group(1)?.trim()
                if (!item.isNullOrEmpty()) {
                    guaranteedSafe.add(item)
                }
            }
        }

        // Extract spam_triggers array of objects
        val spamSectionRegex = Pattern.compile("\"spam_triggers\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL)
        val spamMatcher = spamSectionRegex.matcher(content)
        val spamTriggers = mutableListOf<SpamTriggerItem>()

        if (spamMatcher.find()) {
            val spamBlock = spamMatcher.group(1) ?: ""
            // Match each { ... } object block
            val objMatcher = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL).matcher(spamBlock)
            while (objMatcher.find()) {
                val objContent = objMatcher.group(1) ?: ""
                val trigger = extractString(objContent, "trigger")
                val keyword = extractString(objContent, "keyword")
                val category = extractString(objContent, "category") ?: "FINANCIAL_BAIT"
                if (!trigger.isNullOrEmpty() && !keyword.isNullOrEmpty()) {
                    spamTriggers.add(
                        SpamTriggerItem(
                            trigger = trigger,
                            keyword = keyword,
                            category = category
                        )
                    )
                }
            }
        }

        return LanguageDictionary(
            version = version,
            language = language,
            code = code,
            guaranteedSafe = guaranteedSafe,
            spamTriggers = spamTriggers
        )
    }

    private fun extractString(json: String, key: String): String? {
        val pattern = Pattern.compile("\"$key\"\\s*:\\s*\"([^\"]*)\"")
        val matcher = pattern.matcher(json)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractInt(json: String, key: String): Int? {
        val pattern = Pattern.compile("\"$key\"\\s*:\\s*(\\d+)")
        val matcher = pattern.matcher(json)
        return if (matcher.find()) matcher.group(1)?.toIntOrNull() else null
    }
}
