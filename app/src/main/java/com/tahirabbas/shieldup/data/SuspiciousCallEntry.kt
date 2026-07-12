package com.tahirabbas.shieldup.data

data class SuspiciousCallEntry(
    val id: String,
    val claimedCaller: String,
    val phoneNumber: String,
    val redFlags: List<RedFlag>,
    val notes: String,
    val timestampMillis: Long
)
