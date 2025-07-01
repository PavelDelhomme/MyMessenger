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
    val numberOfMessages: Int = 0,
    val photoUri: String? = null,
    val isPinned: Boolean = false,
    var unreadCount : Int = 0,
    var isMuted: Boolean = false,
    var isBlocked: Boolean = false,
    var isDeleted: Boolean = false,
    var isSpam: Boolean = false,
    var isRcs: Boolean = false,
    var isArchived: Boolean = false,
    var isGroup: Boolean = false,
    var isStarred: Boolean = false,
)
