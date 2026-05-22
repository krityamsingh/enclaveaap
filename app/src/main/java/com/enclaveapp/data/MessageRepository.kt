package com.enclaveapp.data

import kotlinx.coroutines.flow.Flow

class MessageRepository(private val messageDao: MessageDao) {
    val allMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()
    val allConversations: Flow<List<ConversationEntity>> = messageDao.getAllConversations()
    val allContacts: Flow<List<ContactEntity>> = messageDao.getAllContacts()

    fun getMessagesByConversation(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesByConversation(conversationId)
    }

    suspend fun insertContact(contact: ContactEntity) {
        messageDao.insertContact(contact)
    }

    suspend fun markContactVerified(userId: String) {
        messageDao.markContactVerified(userId)
    }

    suspend fun insert(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    suspend fun updateStatus(id: String, status: MessageStatus) {
        messageDao.updateStatus(id, status)
    }

    suspend fun markMessagesAsRead(conversationId: String) {
        messageDao.markMessagesAsRead(conversationId)
    }

    suspend fun deleteExpiredMessages(now: Long) {
        messageDao.deleteExpiredMessages(now)
    }

    suspend fun insertConversation(conversation: ConversationEntity) {
        messageDao.insertConversation(conversation)
    }

    suspend fun updateConversationLastMessage(id: String, lastMessage: String, timestamp: Long) {
        messageDao.updateConversationLastMessage(id, lastMessage, timestamp)
    }

    suspend fun updateConversationTimer(id: String, disappearAfterMs: Long?) {
        messageDao.updateConversationTimer(id, disappearAfterMs)
    }

    suspend fun delete(message: MessageEntity) {
        messageDao.deleteMessage(message)
    }

    suspend fun deleteConversation(id: String) {
        messageDao.deleteConversation(id)
    }

    suspend fun archiveConversation(id: String) {
        messageDao.archiveConversation(id)
    }

    suspend fun toggleMuteConversation(id: String) {
        messageDao.toggleMuteConversation(id)
    }
}
