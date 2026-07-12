package com.tahirabbas.shieldup.data

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class CallLogRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("shieldup_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOG = "suspicious_call_log"
    }

    fun getEntries(): List<SuspiciousCallEntry> {
        val raw = prefs.getString(KEY_LOG, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("##").mapNotNull { entry ->
            val parts = entry.split("|", limit = 5)
            if (parts.size == 5) {
                val flags = parts[3].split(",").filter { it.isNotBlank() }
                    .mapNotNull { flagName -> RedFlag.values().find { it.name == flagName } }
                SuspiciousCallEntry(
                    id = parts[0],
                    claimedCaller = parts[1],
                    phoneNumber = parts[2],
                    redFlags = flags,
                    notes = parts[4].substringBefore("~~"),
                    timestampMillis = parts[4].substringAfter("~~", "0").toLongOrNull() ?: 0L
                )
            } else null
        }.sortedByDescending { it.timestampMillis }
    }

    private fun saveEntries(entries: List<SuspiciousCallEntry>) {
        val raw = entries.joinToString("##") { entry ->
            val flagsStr = entry.redFlags.joinToString(",") { it.name }
            "${entry.id}|${entry.claimedCaller}|${entry.phoneNumber}|$flagsStr|${entry.notes}~~${entry.timestampMillis}"
        }
        prefs.edit().putString(KEY_LOG, raw).apply()
    }

    fun addEntry(
        claimedCaller: String,
        phoneNumber: String,
        redFlags: List<RedFlag>,
        notes: String
    ): SuspiciousCallEntry {
        val entry = SuspiciousCallEntry(
            id = UUID.randomUUID().toString(),
            claimedCaller = claimedCaller,
            phoneNumber = phoneNumber,
            redFlags = redFlags,
            notes = notes,
            timestampMillis = System.currentTimeMillis()
        )
        saveEntries(getEntries() + entry)
        return entry
    }

    fun removeEntry(entry: SuspiciousCallEntry) {
        saveEntries(getEntries().filterNot { it.id == entry.id })
    }
}
