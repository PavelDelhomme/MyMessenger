package com.delhomme.mymessenger.data.repository

import androidx.paging.PagingSource
import com.delhomme.mymessenger.data.local.AppDatabase
import com.delhomme.mymessenger.data.local.MessageEntity

class MessageRepository(private val db: AppDatabase) {
    fun getPagedMessages(conversationId: Long): PagingSource<Int, MessageEntity> {
        return db.messageDao().getMessagesPaging(conversationId)
    }

    suspend fun insertMessages(messages: List<MessageEntity>) {
        db.messageDao().insertMessages(messages)
    }

    suspend fun updateMessageStatus(id: Long, status: String) {
        db.messageDao().updateMessageStatus(id, status)
    }

    fun getMessagesByConversationPaging(conversationId: Long): PagingSource<Int, MessageEntity> {
        return db.messageDao().getMessagesByConversationPaging(conversationId)
    }

    // Nouvelles méthodes
    suspend fun getMessageById(messageId: Long): MessageEntity? {
        return db.messageDao().getMessageById(messageId)
    }

    suspend fun getMessagesText(ids: List<Long>): List<String> {
        return db.messageDao().getMessagesText(ids)
    }

    suspend fun getAllMessagesForConversation(conversationId: Long): List<MessageEntity> {
        return db.messageDao().getAllMessagesForConversation(conversationId)
    }
    suspend fun getAllMessages(): List<MessageEntity> {
        return db.messageDao().getAllMessages()
    }
}
