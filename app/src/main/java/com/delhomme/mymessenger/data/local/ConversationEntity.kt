package com.delhomme.mymessenger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: Long,
    val address: String,
    val lastMessage: String,
    val lastDate: Long,
    val archived: Boolean = false
)