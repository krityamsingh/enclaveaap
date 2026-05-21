package com.enclaveapp.data

import android.content.Context
import com.enclaveapp.crypto.CryptoManager
import com.enclaveapp.crypto.LocalKeyStore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MessagingRepository(private val context: Context) {
    // For now we don't have google-services.json so FirebaseFirestore.getInstance() might crash
    // if Firebase isn't initialized. We can wrap it in a try-catch for demo purposes, 
    // or just let it crash since setting up Firebase properly requires google-services.json
    private val db by lazy { FirebaseFirestore.getInstance() }

    suspend fun sendMessage(
        recipientId: String,
        plaintext: String,
        myUserId: String
    ) {
        val keyDoc = db.collection("publicKeys").document(recipientId).get().await()
        val recipientPublicKeyB64 = keyDoc.getString("publicKey")
            ?: throw Exception("Recipient has no registered public key")
        val recipientPublicKey = android.util.Base64.decode(recipientPublicKeyB64, android.util.Base64.NO_WRAP)

        val myPrivateKey = LocalKeyStore.getPrivateKey(context)
        val payload = CryptoManager.encrypt(plaintext, myPrivateKey, recipientPublicKey)

        val messageData = hashMapOf(
            "payload" to payload,
            "senderId" to myUserId,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("inbox").document(recipientId)
            .collection("messages").add(messageData).await()
    }

    suspend fun fetchAndDecryptInbox(myUserId: String): List<DecryptedMessage> {
        val myPrivateKey = LocalKeyStore.getPrivateKey(context)
        val messages = db.collection("inbox").document(myUserId)
            .collection("messages")
            .orderBy("timestamp")
            .get().await()

        val result = mutableListOf<DecryptedMessage>()

        for (doc in messages.documents) {
            val payload = doc.getString("payload") ?: continue
            val senderId = doc.getString("senderId") ?: continue
            val timestamp = doc.getLong("timestamp") ?: 0L

            val senderKeyDoc = db.collection("publicKeys").document(senderId).get().await()
            val senderPubKeyB64 = senderKeyDoc.getString("publicKey") ?: continue
            val senderPublicKey = android.util.Base64.decode(senderPubKeyB64, android.util.Base64.NO_WRAP)

            val plaintext = CryptoManager.decrypt(payload, myPrivateKey, senderPublicKey)
                ?: continue

            result.add(DecryptedMessage(
                id = doc.id,
                senderId = senderId,
                plaintext = plaintext,
                timestamp = timestamp
            ))

            doc.reference.delete().await()
        }

        return result
    }

    suspend fun registerPublicKey(userId: String, displayName: String) {
        val pubKey = LocalKeyStore.getPublicKeyBase64(context)
        db.collection("publicKeys").document(userId).set(
            hashMapOf("publicKey" to pubKey, "displayName" to displayName)
        ).await()
    }
}

data class DecryptedMessage(
    val id: String,
    val senderId: String,
    val plaintext: String,
    val timestamp: Long
)
