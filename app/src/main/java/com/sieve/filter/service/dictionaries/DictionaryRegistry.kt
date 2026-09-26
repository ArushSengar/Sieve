package com.sieve.filter.service.dictionaries

object DictionaryRegistry {

    private val supportedLanguages = listOf(
        "hindi",
        "tamil",
        "telugu",
        "bengali",
        "marathi",
        "gujarati",
        "punjabi"
    )

    private val dictionaries = mutableMapOf<String, LanguageDictionary>()

    val allGuaranteedSafePhrases: List<String>
    val allSpamTriggers: List<SpamTriggerItem>

    init {
        val safeList = mutableListOf<String>()
        val spamList = mutableListOf<SpamTriggerItem>()

        for (lang in supportedLanguages) {
            val stream = DictionaryRegistry::class.java.getResourceAsStream("/dictionaries/$lang.json")
                ?: DictionaryRegistry::class.java.classLoader?.getResourceAsStream("dictionaries/$lang.json")
                ?: Thread.currentThread().contextClassLoader?.getResourceAsStream("dictionaries/$lang.json")

            if (stream != null) {
                try {
                    val dict = DictionaryParser.parse(stream)
                    dictionaries[dict.code] = dict
                    dictionaries[dict.language] = dict
                    safeList.addAll(dict.guaranteedSafe)
                    spamList.addAll(dict.spamTriggers)
                } catch (e: Exception) {
                    System.err.println("Failed to parse dictionary $lang: ${e.message}")
                }
            } else {
                System.err.println("Dictionary not found: $lang")
            }
        }

        allGuaranteedSafePhrases = safeList.distinct()
        allSpamTriggers = spamList
    }

    fun getDictionary(codeOrName: String): LanguageDictionary? {
        return dictionaries[codeOrName.lowercase()]
    }

    fun getSupportedLanguages(): List<String> = supportedLanguages
}
