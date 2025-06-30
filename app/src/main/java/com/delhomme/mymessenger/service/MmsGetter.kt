package com.delhomme.mymessenger.service

import android.content.Context
import androidx.core.net.toUri
import com.delhomme.mymessenger.data.local.MessageEntity

fun getMmsInbox(context: Context): List<MessageEntity> {
    val mmsList = mutableListOf<MessageEntity>()
    val uri = "content://mms/inbox".toUri() // ✅ Utilisation de .toUri() (KTX)
    val cursor = context.contentResolver.query(uri, null, null, null, "date DESC")

    cursor?.use {
        while (it.moveToNext()) {
            val id = it.getLong(it.getColumnIndexOrThrow("_id"))
            val date = it.getLong(it.getColumnIndexOrThrow("date")) * 1000

            mmsList.add(
                MessageEntity(
                    id = id,
                    conversationId = id,
                    address = "MMS", // Temporaire
                    body = "[MMS reçu]",
                    date = date,
                    isMe = false,
                    type = "mms"
                )
            )
        }
    }
    return mmsList
}
