package com.delhomme.mymessenger.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


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

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Long): ConversationEntity?


    @Query("SELECT * FROM conversations WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getConversationByPhone(phoneNumber: String): ConversationEntity?

    @Query("UPDATE conversations SET lastMessage = :message, lastDate = :date WHERE id = :id")
    suspend fun updateLastMessage(id: Long, message: String, date: Long)


    @Query("SELECT * FROM conversations")
    suspend fun getAllConversations(): List<ConversationEntity>

    @Query("UPDATE conversations SET lastMessage = :message, lastDate = :date, numberOfMessages = :count WHERE id = :id")
    suspend fun updateConversation(id: Long, message: String, date: Long, count: Int)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Delete
    suspend fun deleteConversations(conversations: List<ConversationEntity>)

}
