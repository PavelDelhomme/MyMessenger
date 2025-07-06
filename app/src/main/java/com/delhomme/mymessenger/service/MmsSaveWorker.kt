package com.delhomme.mymessenger.service


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delhomme.mymessenger.MainActivity
import com.delhomme.mymessenger.R
import com.delhomme.mymessenger.data.local.AppDatabase
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.utils.lookupContact
import com.delhomme.mymessenger.utils.normalizePhoneNumber

class MmsSaveWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.build(applicationContext)
        val convDao = db.conversationDao()
        val msgDao = db.messageDao()

        // Récupérer le dernier MMS reçu (inbox)
        val mmsUri = Uri.parse("content://mms")
        val projection = arrayOf("_id", "date", "msg_box")
        val selection = "msg_box=1" // inbox uniquement
        val sortOrder = "date DESC LIMIT 1"
        var mmsId: Long? = null
        var date: Long = 0

        applicationContext.contentResolver.query(
            mmsUri, projection, selection, null, sortOrder
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                mmsId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                date = cursor.getLong(cursor.getColumnIndexOrThrow("date")) * 1000
            }
        }

        if (mmsId == null) return Result.failure()

        // Récupérer l’adresse de l’expéditeur
        var address: String? = null
        val addrUri = Uri.parse("content://mms/$mmsId/addr")
        applicationContext.contentResolver.query(addrUri, arrayOf("address", "type"), null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val type = cursor.getInt(cursor.getColumnIndexOrThrow("type"))
                if (type == 137) { // 137 = sender
                    address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                    if (address != null && address != "insert-address-token") break
                }
            }
        }
        if (address.isNullOrBlank()) return Result.failure()

        // Récupérer le texte du MMS
        var body = ""
        val partUri = Uri.parse("content://mms/$mmsId/part")
        applicationContext.contentResolver.query(partUri, null, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val ct = cursor.getString(cursor.getColumnIndexOrThrow("ct"))
                if (ct == "text/plain") {
                    val partId = cursor.getString(cursor.getColumnIndexOrThrow("_id"))
                    val inputStream = applicationContext.contentResolver.openInputStream(Uri.parse("content://mms/part/$partId"))
                    body = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                    break
                }
            }
        }

        val normalizedPhone = normalizePhoneNumber(address!!)
        val (name, photo) = lookupContact(applicationContext, address!!)

        // Conversation
        var conversation = convDao.getConversationByPhone(normalizedPhone)
        if (conversation == null) {
            conversation = ConversationEntity(
                id = normalizedPhone.hashCode().toLong(),
                phoneNumber = normalizedPhone,
                fullName = name ?: normalizedPhone,
                lastMessage = body,
                lastDate = date,
                numberOfMessages = 1,
                photoUri = photo
            )
            convDao.insertConversations(listOf(conversation))
        } else {
            convDao.updateLastMessage(conversation.id, body, date)
        }

        // Message
        val message = MessageEntity(
            id = mmsId!!,
            conversationId = conversation.id,
            address = address!!,
            body = body,
            date = date,
            isMe = false, // inbox
            type = "mms",
            status = "DELIVERED"
        )
        msgDao.insertMessages(listOf(message))

        // Notification
        showIncomingNotification(applicationContext, conversation.id, name ?: address!!, body)
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

    if (nm.getNotificationChannel(channelId) == null) {
        nm.createNotificationChannel(
            NotificationChannel(
                channelId,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications SMS/MMS"
            }
        )
    }

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