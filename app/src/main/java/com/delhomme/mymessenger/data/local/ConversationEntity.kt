package com.delhomme.mymessenger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: Long,
    val phoneNumber: String,
    var fullName: String,
    val lastMessage: String = "",
    val lastDate: Long = System.currentTimeMillis(),
    val archived: Boolean = false,
    val numberOfMessages: Int = 0,
    val photoUri: String? = null
)
