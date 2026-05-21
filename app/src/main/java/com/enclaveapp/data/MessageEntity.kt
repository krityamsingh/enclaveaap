package com.enclaveapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

import java.util.UUID

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val plaintext: String,           // decrypted text — only on THIS device
    val encryptedPayload: String,    // the ciphertext blob (for re-sending if needed)
    val isSentByMe: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.DELIVERED,
    val senderPublicKeyFingerprint: String = "",
    val expiresAt: Long? = null
)

enum class MessageStatus { QUEUED, SENDING, DELIVERED, FAILED }
