package com.tahirabbas.shieldup.utils

object ClipboardCheckHelper {

    data class ClipboardResult(val isSensitive: Boolean, val reason: String?)

    private val CARD_PATTERN = Regex("\\b(?:\\d[ -]*?){13,16}\\b")
    private val CVV_PATTERN = Regex("\\bcvv\\s*[:=]?\\s*\\d{3,4}\\b", RegexOption.IGNORE_CASE)

    fun check(clipboardText: String): ClipboardResult {
        val text = clipboardText.trim()
        if (text.isBlank()) return ClipboardResult(false, null)

        if (CARD_PATTERN.containsMatchIn(text)) {
            return ClipboardResult(true, "This looks like it could be a card number.")
        }
        if (CVV_PATTERN.containsMatchIn(text)) {
            return ClipboardResult(true, "This looks like it could include a CVV/security code.")
        }
        if (text.length in 6..40 && text.any { it.isDigit() } && text.any { it.isLetter() } &&
            !text.contains(" ") && text.any { !it.isLetterOrDigit() }
        ) {
            return ClipboardResult(true, "This looks like it could be a password (mixed letters, numbers, symbols, no spaces).")
        }
        return ClipboardResult(false, null)
    }
}
