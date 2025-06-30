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
    val type: String, // "sms" ou "mms"
    val status: String = "DELIVERED"
)
