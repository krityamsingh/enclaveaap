package com.enclaveapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enclaveapp.data.AppDatabase
import com.enclaveapp.data.MessageEntity
import com.enclaveapp.data.ConversationEntity
import com.enclaveapp.data.MessageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
// Removed OkHttp and JSON imports


import com.enclaveapp.data.ContactEntity
import com.enclaveapp.data.MessagingRepository
import com.enclaveapp.data.MessageStatus

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MessageRepository
    private val messagingRepository = MessagingRepository(application)
    private val myUserId = "user123_temp" // Hardcoded for demo if not using real auth

    val offlineQueue: StateFlow<List<MessageEntity>>
    val conversations: StateFlow<List<ConversationEntity>>
    val contacts: StateFlow<List<ContactEntity>>

    init {
        val messageDao = AppDatabase.getDatabase(application).messageDao()
        repository = MessageRepository(messageDao)
        offlineQueue = repository.allMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        conversations = repository.allConversations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        contacts = repository.allContacts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        viewModelScope.launch {
            if (repository.allConversations.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()).value.isEmpty()) {
                repository.insertConversation(ConversationEntity("1", "Vikram Sharma", "Sure, let's discuss this tomorrow."))
                repository.insertConversation(ConversationEntity("2", "Dev Ops Production", "Deployment successful."))
                repository.insertConversation(ConversationEntity("3", "System Alerts", "Backup completed securely."))
            }
        }
    }
    
    fun getMessages(conversationId: String): StateFlow<List<MessageEntity>> {
        return repository.getMessagesByConversation(conversationId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun sendMessage(conversationId: String, plaintext: String, recipientId: String, disappearAfterMs: Long? = null) {
        viewModelScope.launch {
            val expiresAt = if (disappearAfterMs != null) System.currentTimeMillis() + disappearAfterMs else null
            
            val localMsg = MessageEntity(
                conversationId = conversationId,
                plaintext = plaintext,
                encryptedPayload = "",  // filled after encryption
                isSentByMe = true,
                status = MessageStatus.QUEUED,
                expiresAt = expiresAt
            )
            repository.insert(localMsg)
            repository.insertConversation(ConversationEntity(conversationId, recipientId, plaintext, disappearAfterMs = disappearAfterMs))

            try {
                messagingRepository.sendMessage(
                    recipientId = recipientId,
                    plaintext = plaintext,
                    myUserId = myUserId
                )
                repository.updateStatus(localMsg.id, MessageStatus.DELIVERED)
            } catch (e: Exception) {
                repository.updateStatus(localMsg.id, MessageStatus.FAILED)
            }
        }
    }

    fun markContactVerified(userId: String) {
        viewModelScope.launch {
            repository.markContactVerified(userId)
        }
    }

    fun deleteMessage(message: MessageEntity) {
        viewModelScope.launch {
            repository.delete(message)
        }
    }
}
