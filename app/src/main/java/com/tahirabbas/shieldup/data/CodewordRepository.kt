package com.tahirabbas.shieldup.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Stores the family's shared secret codeword locally.
 *
 * Deliberately not encrypted with anything fancy — this is a single-device,
 * personal-safety tool, not a password manager. The real security property
 * comes from *never displaying it during a Verify Call flow* (see
 * VerifyCallScreen) — it's only ever shown here, in Setup, where the user
 * explicitly chose to view/edit it.
 */
class CodewordRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("shieldup_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CODEWORD = "family_codeword"
    }

    fun getCodeword(): String? = prefs.getString(KEY_CODEWORD, null)?.takeIf { it.isNotBlank() }

    fun setCodeword(value: String) {
        prefs.edit().putString(KEY_CODEWORD, value.trim()).apply()
    }

    fun isSet(): Boolean = !getCodeword().isNullOrBlank()
}
