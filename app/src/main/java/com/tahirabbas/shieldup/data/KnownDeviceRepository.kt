package com.tahirabbas.shieldup.data

import android.content.Context
import android.content.SharedPreferences

data class KnownDevice(val macAddress: String, val label: String)

class KnownDeviceRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("shieldup_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DEVICES = "known_devices"
    }

    fun getKnownDevices(): List<KnownDevice> {
        val raw = prefs.getString(KEY_DEVICES, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";;").mapNotNull { entry ->
            val parts = entry.split("|", limit = 2)
            if (parts.size == 2) KnownDevice(parts[0], parts[1]) else null
        }
    }

    fun labelDevice(macAddress: String, label: String) {
        val current = getKnownDevices().filterNot { it.macAddress == macAddress }
        val updated = current + KnownDevice(macAddress, label)
        val raw = updated.joinToString(";;") { "${it.macAddress}|${it.label}" }
        prefs.edit().putString(KEY_DEVICES, raw).apply()
    }

    fun isKnown(macAddress: String): Boolean = getKnownDevices().any { it.macAddress == macAddress }

    fun labelFor(macAddress: String): String? = getKnownDevices().firstOrNull { it.macAddress == macAddress }?.label
}
