package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queued_messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipient: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)
