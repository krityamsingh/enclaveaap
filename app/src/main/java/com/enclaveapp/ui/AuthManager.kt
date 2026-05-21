package com.enclaveapp.ui

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

class AuthManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isPinSet(): Boolean {
        return sharedPrefs.contains("pin_hash")
    }

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        sharedPrefs.edit().putString("pin_hash", hash).apply()
    }

    fun verifyPin(pin: String): Boolean {
        if (!isPinSet()) return false
        val hash = hashPin(pin)
        return sharedPrefs.getString("pin_hash", "") == hash
    }

    private fun hashPin(pin: String): String {
        val bytes = pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
