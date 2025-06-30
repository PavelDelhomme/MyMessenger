package com.delhomme.mymessenger.service

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delhomme.mymessenger.data.local.AppDatabase
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.data.repository.MessageRepository


class SmsSaveWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val address = inputData.getString("address") ?: return Result.failure()
        val body = inputData.getString("body") ?: return Result.failure()

        val message = MessageEntity(
            id = System.currentTimeMillis(),
            conversationId = 0L, // À adapter
            address = address,
            body = body,
            date = System.currentTimeMillis(),
            isMe = false,
            type = "sms"
        )

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "messenger_db"
        ).build()

        val repository = MessageRepository(db)
        repository.insertMessages(listOf(message))

        return Result.success()
    }
}
