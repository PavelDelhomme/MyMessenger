package com.delhomme.mymessenger.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.delhomme.mymessenger.data.local.AppDatabase
import com.delhomme.mymessenger.data.local.ConversationEntity


class ConversationRepository(private val db: AppDatabase) {
    fun getPagedConversations() =
        Pager(PagingConfig(pageSize = 20)) { db.conversationDao().getAllConversationsPaging() }.flow

    suspend fun insertConversations(conversations: List<ConversationEntity>) =
        db.conversationDao().insertConversations(conversations)
}