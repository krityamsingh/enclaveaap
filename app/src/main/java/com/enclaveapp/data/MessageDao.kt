package com.enclaveapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesByConversation(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: MessageStatus)

    @Query("DELETE FROM messages WHERE expiresAt IS NOT NULL AND expiresAt < :now")
    suspend fun deleteExpiredMessages(now: Long)

    @Query("UPDATE messages SET status = :status WHERE conversationId = :conversationId AND isSentByMe = 0 AND status != :status")
    suspend fun markMessagesAsRead(conversationId: String, status: MessageStatus = MessageStatus.READ)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY timestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET lastMessage = :lastMessage, timestamp = :timestamp WHERE id = :id")
    suspend fun updateConversationLastMessage(id: String, lastMessage: String, timestamp: Long)

    @Query("UPDATE conversations SET disappearAfterMs = :disappearAfterMs WHERE id = :id")
    suspend fun updateConversationTimer(id: String, disappearAfterMs: Long?)

    @Query("SELECT * FROM contacts")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Query("UPDATE contacts SET isVerified = 1 WHERE userId = :userId")
    suspend fun markContactVerified(userId: String)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("UPDATE conversations SET isArchived = 1 WHERE id = :id")
    suspend fun archiveConversation(id: String)

    @Query("UPDATE conversations SET isMuted = CASE WHEN isMuted = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun toggleMuteConversation(id: String)
}
