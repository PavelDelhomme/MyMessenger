package com.delhomme.mymessenger.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.delhomme.mymessenger.data.local.AppDatabase
import com.delhomme.mymessenger.data.local.MessageEntity


class MessageRepository(private val db: AppDatabase) {
    fun getPagedMessages(conversationId: Long) =
        Pager(PagingConfig(pageSize = 50)) { db.messageDao().getMessagesPaging(conversationId) }.flow

    suspend fun insertMessages(messages: List<MessageEntity>) =
        db.messageDao().insertMessages(messages)
}
