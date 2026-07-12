package com.tahirabbas.shieldup.utils

/**
 * Pattern-based phishing/scam heuristics. Deliberately framed as advisory
 * signals, not a definitive verdict; there is no offline API that can
 * reliably classify a URL as malicious. This catches common, well-known
 * scam patterns and will miss novel ones or false-positive on unusual but
 * legitimate URLs. Always presented to the user as signs to weigh, not a verdict.
 */
object LinkCheckHelper {

    data class LinkCheckResult(
        val warnings: List<String>,
        val riskLevel: RiskLevel,
        val extractedUrl: String?
    )

    enum class RiskLevel { LOW, MEDIUM, HIGH }

    private val URL_REGEX = Regex("(https?://\\S+|www\\.\\S+|\\b[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(?:/\\S*)?)")

    private val SUSPICIOUS_TLDS = listOf(".xyz", ".top", ".click", ".loan", ".work", ".gq", ".tk")
    private val URL_SHORTENERS = listOf("bit.ly", "tinyurl.com", "t.co", "goo.gl", "is.gd", "cutt.ly")
    private val URGENCY_WORDS = listOf(
        "verify now", "act now", "urgent", "suspended", "locked", "claim your",
        "winner", "congratulations", "limited time", "click here immediately",
        "your account will be", "confirm your identity"
    )
    private val IMPERSONATED_BRANDS = listOf("paypal", "facebook", "instagram", "whatsapp", "google", "bank", "amazon", "easypaisa", "jazzcash")

    fun analyze(input: String): LinkCheckResult {
        val fullText = input.trim()
        val lowerText = fullText.lowercase()
        val warnings = mutableListOf<String>()

        val extractedUrl = URL_REGEX.find(fullText)?.value
        val urlLower = extractedUrl?.lowercase() ?: lowerText

        if (SUSPICIOUS_TLDS.any { urlLower.contains(it) }) {
            warnings.add("Uses a domain ending commonly associated with scam sites")
        }
        if (URL_SHORTENERS.any { urlLower.contains(it) }) {
            warnings.add("This is a shortened link, the real destination is hidden")
        }
        if (URGENCY_WORDS.any { lowerText.contains(it) }) {
            warnings.add("Contains urgent or pressure language commonly used in scams")
        }
        val hyphenCount = (extractedUrl ?: "").count { it == '-' }
        if (hyphenCount >= 3) {
            warnings.add("Unusually many hyphens in the domain, a common lookalike-URL trick")
        }
        IMPERSONATED_BRANDS.forEach { brand ->
            if (lowerText.contains(brand) && extractedUrl != null &&
                !urlLower.contains("$brand.com") && !urlLower.contains("$brand.co")
            ) {
                warnings.add("Mentions \"$brand\" but the link doesn't look like their real official domain")
            }
        }
        if (Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}").containsMatchIn(urlLower)) {
            warnings.add("Uses a raw IP address instead of a normal domain name, very unusual for a legitimate site")
        }
        if (extractedUrl == null && URGENCY_WORDS.none { lowerText.contains(it) } && lowerText.isNotBlank()) {
            warnings.add("No link found in this text, only the wording could be checked")
        }

        val risk = when {
            warnings.count { !it.startsWith("No link found") } >= 3 -> RiskLevel.HIGH
            warnings.count { !it.startsWith("No link found") } >= 1 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
        return LinkCheckResult(warnings, risk, extractedUrl)
    }
}
