package com.enclave.secureapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lastMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)
