package com.tahirabbas.shieldup.data

import android.content.Context
import android.content.SharedPreferences

class ApiKeyRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("shieldup_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SAFE_BROWSING = "safe_browsing_api_key"
    }

    fun getSafeBrowsingKey(): String? = prefs.getString(KEY_SAFE_BROWSING, null)?.takeIf { it.isNotBlank() }

    fun setSafeBrowsingKey(value: String) {
        prefs.edit().putString(KEY_SAFE_BROWSING, value.trim()).apply()
    }
}
