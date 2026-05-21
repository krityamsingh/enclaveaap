package com.enclave.secureapp.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.Key

class CryptoManager(context: Context) {
    private val lazySodium = LazySodiumAndroid(SodiumAndroid())
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "crypto_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getOrGenerateKeyPair(): KeyPairStr {
        val pub = sharedPrefs.getString("public_key", null)
        val priv = sharedPrefs.getString("private_key", null)
        
        if (pub != null && priv != null) {
            return KeyPairStr(pub, priv)
        }
        
        // Generate new X25519 keypair for box (crypto_box)
        val keyPair = lazySodium.cryptoBoxKeypair()
        val publicKeyString = keyPair.publicKey.asHexString
        val privateKeyString = keyPair.secretKey.asHexString
        
        sharedPrefs.edit()
            .putString("public_key", publicKeyString)
            .putString("private_key", privateKeyString)
            .apply()
            
        return KeyPairStr(publicKeyString, privateKeyString)
    }
    
    fun getPublicKeyFingerprint(): String {
        val kp = getOrGenerateKeyPair()
        // Format as hex pairs (e.g., 3F9A 28BC)
        val hex = kp.publicKey.uppercase()
        val chunked = hex.chunked(4).joinToString(" ")
        // Return first 16 bytes (32 hex chars = 8 chunks of 4)
        return chunked.split(" ").take(8).chunked(4).joinToString("\n") { it.joinToString(" ") }
    }
}

data class KeyPairStr(val publicKey: String, val privateKey: String)
