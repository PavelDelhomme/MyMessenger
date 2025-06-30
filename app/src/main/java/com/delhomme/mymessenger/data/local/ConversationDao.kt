package com.delhomme.mymessenger.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY lastDate DESC")
    fun getAllConversationsPaging(): PagingSource<Int, ConversationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Query("""
        SELECT * FROM conversations
        WHERE fullName LIKE '%' || :q || '%'
            OR phoneNumber LIKE '%' || :q || '%'
        ORDER BY lastDate DESC
    """)
    fun searchConversations(q: String): PagingSource<Int, ConversationEntity>
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :id")
    suspend fun countMessages(id: Long): Int

}
