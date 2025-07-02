package com.delhomme.mymessenger.data.local

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY date DESC")
    fun getMessagesPaging(convId: Long): PagingSource<Int, MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateMessageStatus(id: Long, status: String)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY date DESC")
    fun getMessagesByConversationPaging(conversationId: Long): PagingSource<Int, MessageEntity>

    @Query("SELECT * FROM messages WHERE body = :content AND address = :address LIMIT 1")
    suspend fun getMessageByContent(content: String, address: String): MessageEntity?

    // Nouvelles méthodes pour les réponses et actions groupées
    @Query("SELECT * FROM messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: Long): MessageEntity?

    @Query("DELETE FROM messages WHERE id IN (:ids)")
    suspend fun deleteMessages(ids: List<Long>)

    @Query("SELECT body FROM messages WHERE id IN (:ids) ORDER BY date ASC")
    suspend fun getMessagesText(ids: List<Long>): List<String>
}
