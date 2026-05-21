package com.enclave.secureapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queued_messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val conversationId: String,
    val recipient: String,
    val messageText: String,
    val status: String = "QUEUED", // QUEUED, SENDING, SENT, FAILED
    val isSentByMe: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
