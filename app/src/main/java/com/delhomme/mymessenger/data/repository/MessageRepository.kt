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
}
