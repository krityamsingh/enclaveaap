package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MessageEntity
import com.example.data.MessageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MessageRepository

    val offlineQueue: StateFlow<List<MessageEntity>>

    init {
        val messageDao = AppDatabase.getDatabase(application).messageDao()
        repository = MessageRepository(messageDao)
        offlineQueue = repository.allMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun queueMessage(recipient: String, messageText: String) {
        viewModelScope.launch {
            repository.insert(MessageEntity(recipient = recipient, messageText = messageText))
        }
    }

    fun deleteMessage(message: MessageEntity) {
        viewModelScope.launch {
            repository.delete(message)
        }
    }
}
