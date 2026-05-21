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

    suspend fun deleteExpiredMessages(now: Long) {
        messageDao.deleteExpiredMessages(now)
    }

    suspend fun insertConversation(conversation: ConversationEntity) {
        messageDao.insertConversation(conversation)
    }

    suspend fun delete(message: MessageEntity) {
        messageDao.deleteMessage(message)
    }
}
