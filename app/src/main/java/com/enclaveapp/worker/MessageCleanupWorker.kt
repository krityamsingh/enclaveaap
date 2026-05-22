package com.enclaveapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enclaveapp.data.AppDatabase
import com.enclaveapp.data.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessageCleanupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val messageDao = AppDatabase.getDatabase(applicationContext).messageDao()
            val repository = MessageRepository(messageDao)
            
            // Delete messages whose expiresAt < current timestamp
            val now = System.currentTimeMillis()
            repository.deleteExpiredMessages(now)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
