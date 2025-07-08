package com.delhomme.mymessenger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: Long,
    val conversationId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val isMe: Boolean,
    val type: String, // "sms", "mms", 'instant'
    val status: String = "DELIVERED",
    val mediaUri: String? = null,
    val mediaType: String? = null, // "image", "video", "audio", "file", "gif", "localisation", "contact",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val contactVCard: String? = null,
    val replyToId: Long? = null
)
