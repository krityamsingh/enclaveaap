package com.enclave.secureapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enclave.secureapp.data.AppDatabase
import com.enclave.secureapp.data.MessageEntity
import com.enclave.secureapp.data.ConversationEntity
import com.enclave.secureapp.data.MessageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
// Removed OkHttp and JSON imports


class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MessageRepository

    val offlineQueue: StateFlow<List<MessageEntity>>
    val conversations: StateFlow<List<ConversationEntity>>

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

    fun queueMessage(recipient: String, messageText: String, conversationId: String) {
        viewModelScope.launch {
            val message = MessageEntity(recipient = recipient, messageText = messageText, conversationId = conversationId, status = "QUEUED", isSentByMe = true)
            repository.insert(message)
            repository.insertConversation(ConversationEntity(conversationId, recipient, messageText))
        }
    }

    fun deleteMessage(message: MessageEntity) {
        viewModelScope.launch {
            repository.delete(message)
        }
    }
}
