package com.tahirabbas.shieldup.data

import android.content.Context
import android.content.SharedPreferences

class FamilyContactRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("shieldup_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CONTACTS = "family_contacts"
        const val MAX_CONTACTS = 8
    }

    fun getContacts(): List<FamilyContact> {
        val raw = prefs.getString(KEY_CONTACTS, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";;").mapNotNull { entry ->
            val parts = entry.split("|", limit = 3)
            if (parts.size == 3) FamilyContact(parts[0], parts[1], parts[2]) else null
        }
    }

    fun saveContacts(contacts: List<FamilyContact>) {
        val limited = contacts.take(MAX_CONTACTS)
        val raw = limited.joinToString(";;") { "${it.name}|${it.relation}|${it.phoneNumber}" }
        prefs.edit().putString(KEY_CONTACTS, raw).apply()
    }

    fun addContact(contact: FamilyContact): Boolean {
        val current = getContacts()
        if (current.size >= MAX_CONTACTS) return false
        saveContacts(current + contact)
        return true
    }

    fun removeContact(contact: FamilyContact) {
        saveContacts(getContacts().filterNot { it.phoneNumber == contact.phoneNumber })
    }
}
