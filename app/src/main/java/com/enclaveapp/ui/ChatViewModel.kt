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
import com.enclaveapp.data.GiphyApiService
import com.enclaveapp.data.GiphyGif
import com.enclaveapp.BuildConfig
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MessageRepository
    private val messagingRepository = MessagingRepository(application)
    private val myUserId = "user123_temp" // Hardcoded for demo if not using real auth

    val offlineQueue: StateFlow<List<MessageEntity>>
    val conversations: StateFlow<List<ConversationEntity>>
    val contacts: StateFlow<List<ContactEntity>>

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.giphy.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val giphyService = retrofit.create(GiphyApiService::class.java)

    private val _gifResults = MutableStateFlow<List<GiphyGif>>(emptyList())
    val gifResults = _gifResults.asStateFlow()
    
    private val _isSearchGifLoading = MutableStateFlow(false)
    val isSearchGifLoading = _isSearchGifLoading.asStateFlow()

    fun searchGifs(query: String) {
        if (query.isBlank()) {
            _gifResults.value = emptyList()
            return
        }
        _isSearchGifLoading.value = true
        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GIPHY_API_KEY
                if (apiKey.isNotBlank() && apiKey != "YOUR_GIPHY_API_KEY") {
                    val response = giphyService.searchGifs(apiKey, query)
                    _gifResults.value = response.data
                }
            } catch(e: Exception) {
                e.printStackTrace()
            } finally {
                _isSearchGifLoading.value = false
            }
        }
    }
    
    fun clearGifs() {
        _gifResults.value = emptyList()
    }

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
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getMessages(conversationId: String): StateFlow<List<MessageEntity>> {
        return kotlinx.coroutines.flow.combine(
            repository.getMessagesByConversation(conversationId),
            _searchQuery
        ) { messages, query ->
            if (query.isBlank()) {
                messages
            } else {
                messages.filter { it.plaintext.contains(query, ignoreCase = true) }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            repository.markMessagesAsRead(conversationId)
        }
    }

    fun updateConversationTimer(conversationId: String, disappearAfterMs: Long?) {
        viewModelScope.launch {
            repository.updateConversationTimer(conversationId, disappearAfterMs)
        }
    }

    fun sendMessage(conversationId: String, plaintext: String, recipientId: String, customDisappearAfterMs: Long? = null) {
        viewModelScope.launch {
            val conv = conversations.value.find { it.id == conversationId }
            val disappearTimer = customDisappearAfterMs ?: conv?.disappearAfterMs
            val expiresAt = if (disappearTimer != null) System.currentTimeMillis() + disappearTimer else null
            
            val localMsg = MessageEntity(
                conversationId = conversationId,
                plaintext = plaintext,
                encryptedPayload = "",  // filled after encryption
                isSentByMe = true,
                status = MessageStatus.QUEUED,
                expiresAt = expiresAt
            )
            repository.insert(localMsg)
            repository.updateConversationLastMessage(conversationId, plaintext, System.currentTimeMillis())

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

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
        }
    }

    fun archiveConversation(conversationId: String) {
        viewModelScope.launch {
            repository.archiveConversation(conversationId)
        }
    }

    fun toggleMuteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.toggleMuteConversation(conversationId)
        }
    }
}
