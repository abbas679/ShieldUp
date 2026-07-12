package com.tahirabbas.shieldup.utils

object ClipboardCheckHelper {

    data class ClipboardResult(val isSensitive: Boolean, val reason: String?)

    private val CARD_PATTERN = Regex("\\b(?:\\d[ -]*?){13,16}\\b")
    private val CVV_PATTERN = Regex("\\bcvv\\s*[:=]?\\s*\\d{3,4}\\b", RegexOption.IGNORE_CASE)
    private val OTP_PATTERN = Regex("\\b\\d{4,8}\\b.{0,20}\\b(otp|code|verification)\\b", RegexOption.IGNORE_CASE)
    private val EMAIL_PATTERN = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")

    fun check(clipboardText: String): ClipboardResult {
        val text = clipboardText.trim()
        if (text.isBlank()) return ClipboardResult(false, null)

        if (CARD_PATTERN.containsMatchIn(text)) {
            return ClipboardResult(true, "This looks like it could be a card number.")
        }
        if (CVV_PATTERN.containsMatchIn(text)) {
            return ClipboardResult(true, "This looks like it could include a CVV or security code.")
        }
        if (OTP_PATTERN.containsMatchIn(text)) {
            return ClipboardResult(true, "This looks like it could be a one-time passcode. Never share this with anyone, even someone claiming to be your bank.")
        }
        if (text.length in 6..40 && text.any { it.isDigit() } && text.any { it.isLetter() } &&
            !text.contains(" ") && text.any { !it.isLetterOrDigit() }
        ) {
            return ClipboardResult(true, "This looks like it could be a password: mixed letters, numbers, symbols, no spaces.")
        }
        if (EMAIL_PATTERN.containsMatchIn(text) && text.length < 60) {
            return ClipboardResult(true, "This looks like an email address. Only share it with people or forms you trust.")
        }
        return ClipboardResult(false, null)
    }

    /** Masks all but the first and last couple characters, for a safe on-screen preview. */
    fun maskForPreview(text: String): String {
        val trimmed = text.trim()
        if (trimmed.length <= 4) return "*".repeat(trimmed.length)
        val visibleStart = trimmed.take(2)
        val visibleEnd = trimmed.takeLast(2)
        val maskedMiddle = "*".repeat((trimmed.length - 4).coerceAtMost(20))
        return "$visibleStart$maskedMiddle$visibleEnd"
    }
}
