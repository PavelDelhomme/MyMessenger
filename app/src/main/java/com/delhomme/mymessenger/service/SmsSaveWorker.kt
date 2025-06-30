package com.delhomme.mymessenger.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delhomme.mymessenger.MainActivity
import com.delhomme.mymessenger.R
import com.delhomme.mymessenger.data.local.AppDatabase
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.data.repository.MessageRepository
import com.delhomme.mymessenger.utils.lookupContact
import kotlin.jvm.java


class SmsSaveWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val address = inputData.getString("address") ?: return Result.failure()
        val body = inputData.getString("body") ?: return Result.failure()

        val (name, photo) = lookupContact(applicationContext, address)

        val db = AppDatabase.build(applicationContext)
        val convDao = db.conversationDao()
        val msgDao  = db.messageDao()

        val convId = address.hashCode().toLong()

        val message = MessageEntity(
            id = System.currentTimeMillis(),
            conversationId = convId,
            address = address,
            body = body,
            date = System.currentTimeMillis(),
            isMe = false,
            type = "sms"
        )
        msgDao.insertMessages(listOf(message))

        val oldCount = convDao.countMessages(convId)   // ajoute une fonction @Query COUNT
        val conv = ConversationEntity(
            id = convId,
            address = address,
            fullName = name,
            phoneNumber = address,
            photoUri = photo,
            numberOfMessages = oldCount + 1,
            lastMessage = body,
            lastDate = System.currentTimeMillis()
        )
        convDao.insertConversations(listOf(conv))

        showIncomingNotification(applicationContext, convId, name, body)
        return Result.success()
    }
}

private fun showIncomingNotification(
    context: Context,
    convId: Long,
    senderName: String,
    body: String
) {
    val channelId = "messages"
    val nm = context.getSystemService(NotificationManager::class.java)

    // crée le canal une seule fois
    if (nm.getNotificationChannel(channelId) == null) {
        nm.createNotificationChannel(
            NotificationChannel(
                channelId,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications SMS"
            }
        )
    }

    // ouvre la conversation quand l’utilisateur touche la notif
    val contentIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java)
            .putExtra("conversationId", convId),
        PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_message)
        .setContentTitle(senderName)
        .setContentText(body.take(40))
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setContentIntent(contentIntent)
        .setAutoCancel(true)
        .build()

    nm.notify(convId.hashCode(), notification)
}