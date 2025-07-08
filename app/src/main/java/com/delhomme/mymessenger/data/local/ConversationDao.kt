package com.delhomme.mymessenger.data.local

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface ConversationDao {

    @Query("""
        SELECT * FROM conversations 
        WHERE isDeleted = 0 AND isArchived = 0
        ORDER BY 
            isPinned DESC,
            lastDate DESC
    """)
    fun getAllConversationsPaging(): PagingSource<Int, ConversationEntity>

    @Query("""
        SELECT * FROM conversations
        WHERE isDeleted = 0 AND isArchived = 0
        AND (fullName LIKE '%' || :q || '%' OR phoneNumber LIKE '%' || :q || '%')
        ORDER BY 
            isPinned DESC,
            lastDate DESC
    """)
    fun searchConversations(q: String): PagingSource<Int, ConversationEntity>

    @Query("""
        SELECT * FROM conversations 
        WHERE isDeleted = 0 AND isArchived = 1
        ORDER BY lastDate DESC
    """)
    fun getArchivedConversations(): PagingSource<Int, ConversationEntity>

    @Query("""
        SELECT * FROM conversations 
        WHERE isDeleted = 0 AND isBlocked = 1
        ORDER BY lastDate DESC
    """)
    fun getBlockedConversations(): PagingSource<Int, ConversationEntity>

    @Query("""
        SELECT * FROM conversations 
        WHERE isDeleted = 0 AND unreadCount > 0
        ORDER BY 
            isPinned DESC,
            lastDate DESC
    """)
    fun getUnreadConversations(): PagingSource<Int, ConversationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :id")
    suspend fun countMessages(id: Long): Int

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getConversationByPhone(phoneNumber: String): ConversationEntity?

    @Query("UPDATE conversations SET lastMessage = :message, lastDate = :date WHERE id = :id")
    suspend fun updateLastMessage(id: Long, message: String, date: Long)

    @Query("SELECT * FROM conversations WHERE isDeleted = 0")
    suspend fun getAllConversations(): List<ConversationEntity>

    @Query("""
        UPDATE conversations 
        SET lastMessage = :message, lastDate = :date, numberOfMessages = :count 
        WHERE id = :id
    """)
    suspend fun updateConversation(id: Long, message: String, date: Long, count: Int)

    @Delete
    suspend fun deleteConversations(conversations: List<ConversationEntity>)

    // Nouvelles méthodes pour les actions
    @Query("UPDATE conversations SET isPinned = :pinned WHERE id = :id")
    suspend fun updatePinStatus(id: Long, pinned: Boolean)

    @Query("UPDATE conversations SET isArchived = :archived WHERE id = :id")
    suspend fun updateArchiveStatus(id: Long, archived: Boolean)

    @Query("UPDATE conversations SET isMuted = :muted WHERE id = :id")
    suspend fun updateMuteStatus(id: Long, muted: Boolean)

    @Query("UPDATE conversations SET isBlocked = :blocked WHERE id = :id")
    suspend fun updateBlockStatus(id: Long, blocked: Boolean)

    @Query("UPDATE conversations SET isDeleted = :deleted WHERE id = :id")
    suspend fun updateDeleteStatus(id: Long, deleted: Boolean)

    @Query("UPDATE conversations SET unreadCount = :count WHERE id = :id")
    suspend fun updateUnreadCount(id: Long, count: Int)

    // Statistiques
    @Query("SELECT COUNT(*) FROM conversations WHERE isDeleted = 0 AND isArchived = 0")
    suspend fun getActiveConversationsCount(): Int

    @Query("SELECT COUNT(*) FROM conversations WHERE isDeleted = 0 AND unreadCount > 0")
    suspend fun getUnreadConversationsCount(): Int

    @Query("SELECT SUM(unreadCount) FROM conversations WHERE isDeleted = 0")
    suspend fun getTotalUnreadCount(): Int

    // Nettoyage
    @Query("DELETE FROM conversations WHERE isDeleted = 1 AND lastDate < :cutoffDate")
    suspend fun cleanupDeletedConversations(cutoffDate: Long)

    @Query("SELECT * FROM conversations WHERE numberOfMessages = 0")
    suspend fun getEmptyConversations(): List<ConversationEntity>
}