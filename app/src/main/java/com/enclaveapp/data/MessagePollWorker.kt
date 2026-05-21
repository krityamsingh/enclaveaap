package com.enclaveapp.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MessagePollWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return try {
            val myUserId = "user123_temp" // Hardcoded for demo if not using real auth
            val repo = MessagingRepository(applicationContext)
            val appDatabase = AppDatabase.getDatabase(applicationContext)
            val messageDao = appDatabase.messageDao()
            val messageRepo = com.enclaveapp.data.MessageRepository(messageDao)

            messageRepo.deleteExpiredMessages(System.currentTimeMillis())

            val messages = repo.fetchAndDecryptInbox(myUserId)
            for (msg in messages) {
                // save to Room
                val localMsg = MessageEntity(
                    conversationId = msg.senderId, // simplified: using senderId as conversationId
                    plaintext = msg.plaintext,
                    encryptedPayload = "", // Or save encrypted payload if you really want
                    isSentByMe = false,
                    status = MessageStatus.DELIVERED,
                    timestamp = msg.timestamp
                )
                messageDao.insertMessage(localMsg)
                // save conversation
                messageDao.insertConversation(ConversationEntity(msg.senderId, msg.senderId, msg.plaintext))
                
                showNotification(msg.senderId)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(senderName: String) {
        val channelId = "enclave_messages"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Secure Messages"
            val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("New message")
            .setContentText("From $senderName")  // sender name only, per Phase 6
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // Missing POST_NOTIFICATIONS permission
        }
    }
}
