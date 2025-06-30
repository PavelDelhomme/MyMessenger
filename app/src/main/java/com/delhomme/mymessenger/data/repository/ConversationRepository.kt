package com.delhomme.mymessenger.data.repository

import androidx.paging.PagingSource
import com.delhomme.mymessenger.data.local.AppDatabase
import com.delhomme.mymessenger.data.local.ConversationEntity

class ConversationRepository(private val db: AppDatabase) {
    fun getPagedConversations(): PagingSource<Int, ConversationEntity> {
        return db.conversationDao().getAllConversationsPaging()
    }
    fun pagedConversations(query: String) =
        if (query.isBlank()) db.conversationDao().getAllConversationsPaging()
        else db.conversationDao().searchConversations(query)

}
