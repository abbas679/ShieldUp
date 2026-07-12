package com.tahirabbas.shieldup.data

/**
 * A plain checklist of known scam-call warning signs. This is deliberately
 * NOT presented as "AI-detected fraud" — real-time deepfake/clone-voice
 * detection from audio alone is still an unsolved, actively-researched
 * problem, and a portfolio app claiming to reliably do that would be
 * overstating what's actually possible. This is honest decision support:
 * known patterns scam calls commonly share, for the user to weigh themselves.
 */
enum class RedFlag(val description: String) {
    UNKNOWN_NUMBER("Calling from an unknown or unusual number"),
    URGENT_MONEY("Asking urgently for money, gift cards, or a bank transfer"),
    SECRECY("Asking you to keep the call secret from other family members"),
    UNUSUAL_VOICE("Voice sounds slightly off — pacing, tone, or background noise feels wrong"),
    WRONG_DETAIL("Got a personal detail wrong (name, pet, recent event) when asked"),
    PRESSURE_NO_HANGUP("Pressuring you not to hang up or call them back")
}
