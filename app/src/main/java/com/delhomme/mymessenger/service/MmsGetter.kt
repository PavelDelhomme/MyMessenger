package com.delhomme.mymessenger.service

import android.content.Context
import android.net.Uri
import com.delhomme.mymessenger.data.local.MessageEntity

fun getMmsInbox(context: Context): List<MessageEntity> {
    val mmsList = mutableListOf<MessageEntity>()
    val uri = Uri.parse("content://mms/inbox")
    val cursor = context.contentResolver.query(uri, null, null, null, "date DESC")

    cursor?.use {
        while (it.moveToNext()) {
            val id = it.getLong(it.getColumnIndexOrThrow("_id"))
            val date = it.getLong(it.getColumnIndexOrThrow("date")) * 1000


            // Parleer le corps et les pièces jointes ici (voir doc MMS)
            mmsList.add(
                MessageEntity(
                    id = id,
                    conversationId = id,
                    address = "MMS", // A améliorer
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