package com.tahirabbas.shieldup.utils

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.InetAddress

data class LanDevice(val ipAddress: String, val macAddress: String)

/**
 * Scans your own home WiFi network only — this is standard local-network
 * discovery (the same thing your router's admin page already shows you),
 * not remote/internet scanning, and requires no special permissions beyond
 * normal WiFi/network state access.
 */
object WifiScanHelper {

    fun getLocalSubnetPrefix(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return null
        val ipInt = wifiManager.connectionInfo?.ipAddress ?: return null
        if (ipInt == 0) return null
        val ip = String.format(
            "%d.%d.%d.%d",
            ipInt and 0xff, ipInt shr 8 and 0xff, ipInt shr 16 and 0xff, ipInt shr 24 and 0xff
        )
        return ip.substringBeforeLast(".")
    }

    fun getGatewayIp(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return null
        val gatewayInt = wifiManager.dhcpInfo?.gateway ?: return null
        if (gatewayInt == 0) return null
        return String.format(
            "%d.%d.%d.%d",
            gatewayInt and 0xff, gatewayInt shr 8 and 0xff, gatewayInt shr 16 and 0xff, gatewayInt shr 24 and 0xff
        )
    }

    /** Pings every address in the /24 subnet in parallel to find active devices. */
    suspend fun scanSubnet(subnetPrefix: String, timeoutMs: Int = 400): List<String> = withContext(Dispatchers.IO) {
        (1..254).map { host ->
            async {
                val address = "$subnetPrefix.$host"
                try {
                    if (InetAddress.getByName(address).isReachable(timeoutMs)) address else null
                } catch (e: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    /**
     * Reads the kernel ARP table to map discovered IPs to MAC addresses.
     * This file is world-readable on Android and requires no special permission.
     */
    fun readArpTable(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        try {
            val file = File("/proc/net/arp")
            if (!file.exists()) return result
            BufferedReader(FileReader(file)).useLines { lines ->
                lines.drop(1).forEach { line ->
                    val parts = line.trim().split(Regex("\\s+"))
                    if (parts.size >= 4) {
                        val ip = parts[0]
                        val mac = parts[3]
                        if (mac != "00:00:00:00:00:00") result[ip] = mac
                    }
                }
            }
        } catch (e: Exception) {
            // ARP table read is best-effort; scan results still work without it.
        }
        return result
    }

    suspend fun scanForDevices(context: Context): List<LanDevice> {
        val subnet = getLocalSubnetPrefix(context) ?: return emptyList()
        val activeIps = scanSubnet(subnet)
        val arpTable = readArpTable()
        return activeIps.map { ip -> LanDevice(ip, arpTable[ip] ?: "Unknown") }
    }
}
