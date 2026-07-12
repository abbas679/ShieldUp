package com.tahirabbas.shieldup.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Checks a URL against Google's Safe Browsing threat database — the same
 * live, constantly-updated list Chrome and Android itself use to block
 * malware and phishing sites. This is what actually catches real malicious
 * links; local pattern heuristics alone cannot, since attackers constantly
 * rotate through fresh, ordinary-looking domains.
 *
 * Requires a free API key from Google Cloud Console (enable "Safe Browsing
 * API" on a project, generate a key) — set via ApiKeyRepository / Settings.
 */
object SafeBrowsingHelper {

    sealed class SafeBrowsingResult {
        object NotConfigured : SafeBrowsingResult()
        object Safe : SafeBrowsingResult()
        data class Flagged(val threatTypes: List<String>) : SafeBrowsingResult()
        data class Error(val message: String) : SafeBrowsingResult()
    }

    suspend fun checkUrl(url: String, apiKey: String?): SafeBrowsingResult = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrBlank()) return@withContext SafeBrowsingResult.NotConfigured

        try {
            val endpoint = URL("https://safebrowsing.googleapis.com/v4/threatMatches:find?key=$apiKey")
            val connection = endpoint.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            val body = buildRequestBody(url)
            connection.outputStream.use { it.write(body.toByteArray()) }

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                return@withContext SafeBrowsingResult.Error("Safe Browsing check failed (code $responseCode). Your API key may be invalid or the quota was hit.")
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            if (!json.has("matches")) {
                return@withContext SafeBrowsingResult.Safe
            }

            val matches = json.getJSONArray("matches")
            val threatTypes = mutableListOf<String>()
            for (i in 0 until matches.length()) {
                threatTypes.add(matches.getJSONObject(i).optString("threatType", "UNKNOWN"))
            }
            SafeBrowsingResult.Flagged(threatTypes)
        } catch (e: Exception) {
            SafeBrowsingResult.Error("Couldn't reach Safe Browsing right now: ${e.message ?: "unknown error"}")
        }
    }

    private fun buildRequestBody(url: String): String {
        val threatEntry = JSONObject().put("url", url)
        val threatEntries = JSONArray().put(threatEntry)

        val threatInfo = JSONObject()
            .put("threatTypes", JSONArray(listOf("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION")))
            .put("platformTypes", JSONArray(listOf("ANY_PLATFORM")))
            .put("threatEntryTypes", JSONArray(listOf("URL")))
            .put("threatEntries", threatEntries)

        val client = JSONObject()
            .put("clientId", "shieldup-personal-app")
            .put("clientVersion", "1.0")

        return JSONObject()
            .put("client", client)
            .put("threatInfo", threatInfo)
            .toString()
    }

    /** Human-readable label for a threat type code, for display in the UI. */
    fun describeThreatType(type: String): String = when (type) {
        "MALWARE" -> "Malware"
        "SOCIAL_ENGINEERING" -> "Phishing / social engineering"
        "UNWANTED_SOFTWARE" -> "Unwanted software"
        "POTENTIALLY_HARMFUL_APPLICATION" -> "Potentially harmful application"
        else -> type
    }
}
