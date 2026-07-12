package com.tahirabbas.shieldup.utils

/**
 * Pattern-based phishing/scam link heuristics. Deliberately framed as
 * advisory signals, not a definitive verdict — there's no local API that
 * can reliably classify a URL as malicious offline. This catches common,
 * well-known scam patterns; it will miss novel ones and can false-positive
 * on legitimate unusual URLs. Always presented to the user as "signs to
 * consider," never as "this IS a scam."
 */
object LinkCheckHelper {

    data class LinkCheckResult(val warnings: List<String>, val riskLevel: RiskLevel)

    enum class RiskLevel { LOW, MEDIUM, HIGH }

    private val SUSPICIOUS_TLDS = listOf(".xyz", ".top", ".click", ".loan", ".work", ".gq", ".tk")
    private val URL_SHORTENERS = listOf("bit.ly", "tinyurl.com", "t.co", "goo.gl", "is.gd", "cutt.ly")
    private val URGENCY_WORDS = listOf(
        "verify now", "act now", "urgent", "suspended", "locked", "claim your",
        "winner", "congratulations", "limited time", "click here immediately"
    )
    // Common brands frequently impersonated — flags near-miss spellings.
    private val IMPERSONATED_BRANDS = listOf("paypal", "facebook", "instagram", "whatsapp", "google", "bank", "amazon")

    fun analyze(input: String): LinkCheckResult {
        val text = input.trim().lowercase()
        val warnings = mutableListOf<String>()

        if (SUSPICIOUS_TLDS.any { text.contains(it) }) {
            warnings.add("Uses a domain ending commonly associated with scam sites")
        }
        if (URL_SHORTENERS.any { text.contains(it) }) {
            warnings.add("This is a shortened link — the real destination is hidden")
        }
        if (URGENCY_WORDS.any { text.contains(it) }) {
            warnings.add("Contains urgent/pressure language commonly used in scams")
        }
        if (text.count { it == '-' } >= 3) {
            warnings.add("Unusually many hyphens in the domain — a common lookalike-URL trick")
        }
        IMPERSONATED_BRANDS.forEach { brand ->
            if (text.contains(brand) && !text.contains("$brand.com") && !text.contains("$brand.co")) {
                warnings.add("Mentions \"$brand\" but doesn't look like their real official domain")
            }
        }
        if (Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}").containsMatchIn(text)) {
            warnings.add("Uses a raw IP address instead of a normal domain name — very unusual for a legitimate site")
        }

        val risk = when {
            warnings.size >= 3 -> RiskLevel.HIGH
            warnings.size >= 1 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
        return LinkCheckResult(warnings, risk)
    }
}
