package com.delhomme.mymessenger.utils

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.data.repository.MessageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SmsSyncWorker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageRepository: MessageRepository,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) { // Changé en CoroutineWorker

    override suspend fun doWork(): Result { // Maintenant suspend
        val smsList = context.contentResolver.query(
            Uri.parse("content://sms/"),
            null,
            null,
            null,
            null
        )?.use { cursor ->
            val smsList = mutableListOf<MessageEntity>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val address = cursor.getString(2)
                val body = cursor.getString(11)
                val date = cursor.getLong(4)
                val type = if (cursor.getInt(9) == 1) "inbox" else "sent"

                smsList.add(
                    MessageEntity(
                        id = id,
                        conversationId = 0, // Remplacer par l'ID réel plus tard
                        address = address,
                        body = body,
                        date = date,
                        isMe = type == "sent",
                        type = "sms"
                    )
                )
            }
            smsList
        }

        if (smsList != null) {
            // Appel suspendu dans un contexte suspendu
            messageRepository.insertMessages(smsList)
        }
        return Result.success()
    }
}
