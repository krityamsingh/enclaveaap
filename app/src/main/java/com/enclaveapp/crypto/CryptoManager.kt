package com.enclaveapp.crypto

import com.iwebpp.crypto.TweetNaclFast
import android.util.Base64

object CryptoManager {
    fun generateKeypair(): Pair<ByteArray, ByteArray> {
        val kp = TweetNaclFast.Box.keyPair()
        return Pair(kp.publicKey, kp.secretKey)
    }

    fun encrypt(plaintext: String, myPrivateKey: ByteArray, theirPublicKey: ByteArray): String {
        val box = TweetNaclFast.Box(theirPublicKey, myPrivateKey)
        val nonce = TweetNaclFast.makeBoxNonce()   // 24 random bytes
        val ciphertext = box.box(plaintext.toByteArray(Charsets.UTF_8), nonce)
            ?: throw IllegalStateException("Encryption failed")
        val n64 = Base64.encodeToString(nonce, Base64.NO_WRAP)
        val c64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        return "$n64.$c64"
    }

    fun decrypt(payload: String, myPrivateKey: ByteArray, theirPublicKey: ByteArray): String? {
        val parts = payload.split(".")
        if (parts.size != 2) return null
        val nonce = Base64.decode(parts[0], Base64.NO_WRAP)
        val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
        val box = TweetNaclFast.Box(theirPublicKey, myPrivateKey)
        val plain = box.open(ciphertext, nonce) ?: return null
        return String(plain, Charsets.UTF_8)
    }

    fun fingerprint(publicKey: ByteArray): String {
        return publicKey.toList()
            .chunked(4)
            .take(4)
            .joinToString("  ") { chunk -> chunk.joinToString("") { "%02X".format(it) } }
    }
}
