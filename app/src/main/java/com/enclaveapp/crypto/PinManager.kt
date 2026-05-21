package com.enclaveapp.crypto

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

object PinManager {
    private const val PREFS = "enclave_pin"
    private const val KEY_HASH = "pin_hash"
    private const val KEY_SET  = "pin_set"
    private const val SALT = "enclave_v1_salt"

    private fun prefs(ctx: Context) = EncryptedSharedPreferences.create(
        ctx, PREFS,
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun hash(pin: String) = MessageDigest.getInstance("SHA-256")
        .digest("$SALT$pin".toByteArray()).joinToString("") { "%02x".format(it) }

    fun isPinSet(ctx: Context) = prefs(ctx).getBoolean(KEY_SET, false)
    fun setPin(ctx: Context, pin: String) = prefs(ctx).edit()
        .putString(KEY_HASH, hash(pin)).putBoolean(KEY_SET, true).apply()
    fun verifyPin(ctx: Context, pin: String) =
        prefs(ctx).getString(KEY_HASH, null) == hash(pin)
}
