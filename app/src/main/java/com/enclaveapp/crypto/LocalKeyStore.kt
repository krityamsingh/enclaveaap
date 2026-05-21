package com.enclaveapp.crypto

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.util.Base64

object LocalKeyStore {
    private const val PREFS = "enclave_keystore"
    private const val KEY_PUB = "identity_public_key"
    private const val KEY_PRIV = "identity_private_key"

    private fun prefs(ctx: Context) = EncryptedSharedPreferences.create(
        ctx,
        PREFS,
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun hasKeypair(ctx: Context): Boolean =
        prefs(ctx).contains(KEY_PUB)

    fun generateAndSave(ctx: Context) {
        val (pub, priv) = CryptoManager.generateKeypair()
        prefs(ctx).edit()
            .putString(KEY_PUB, Base64.encodeToString(pub, Base64.NO_WRAP))
            .putString(KEY_PRIV, Base64.encodeToString(priv, Base64.NO_WRAP))
            .apply()
    }

    fun getPublicKey(ctx: Context): ByteArray =
        Base64.decode(prefs(ctx).getString(KEY_PUB, null)!!, Base64.NO_WRAP)

    fun getPrivateKey(ctx: Context): ByteArray =
        Base64.decode(prefs(ctx).getString(KEY_PRIV, null)!!, Base64.NO_WRAP)

    fun getPublicKeyBase64(ctx: Context): String =
        prefs(ctx).getString(KEY_PUB, null)!!
}
