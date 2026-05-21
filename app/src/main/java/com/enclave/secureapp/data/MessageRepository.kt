package com.enclave.secureapp.data

import kotlinx.coroutines.flow.Flow

class MessageRepository(private val messageDao: MessageDao) {
    val allMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()
    val allConversations: Flow<List<ConversationEntity>> = messageDao.getAllConversations()

    fun getMessagesByConversation(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesByConversation(conversationId)
    }

    suspend fun insert(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    suspend fun insertConversation(conversation: ConversationEntity) {
        messageDao.insertConversation(conversation)
    }

    suspend fun delete(message: MessageEntity) {
        messageDao.deleteMessage(message)
    }
}
