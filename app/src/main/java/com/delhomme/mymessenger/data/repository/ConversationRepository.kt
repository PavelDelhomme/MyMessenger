package com.delhomme.mymessenger.data.repository

import android.content.Context
import android.provider.Telephony
import androidx.paging.PagingSource
import com.delhomme.mymessenger.data.local.AppDatabase
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.utils.formatFrenchPhoneNumber
import com.delhomme.mymessenger.utils.lookupContact
import com.delhomme.mymessenger.utils.normalizePhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConversationRepository(private val db: AppDatabase) {
    fun getPagedConversations(): PagingSource<Int, ConversationEntity> {
        return db.conversationDao().getAllConversationsPaging()
    }
    fun pagedConversations(query: String) =
        if (query.isBlank()) db.conversationDao().getAllConversationsPaging()
        else db.conversationDao().searchConversations(query)

    suspend fun getConversationById(id: Long): ConversationEntity? {
        return db.conversationDao().getConversationById(id)
    }
    suspend fun getConversationByPhone(phoneNumber: String) : ConversationEntity? {
        return db.conversationDao().getConversationByPhone(phoneNumber)
    }

    suspend fun insertConversation(conversation: ConversationEntity) {
        db.conversationDao().insertConversations(listOf(conversation))
    }

    suspend fun updateConversationLastMessage(conversationId: Long, lastMessage: String, lastDate: Long) {
        db.conversationDao().updateLastMessage(conversationId, lastMessage, lastDate)
    }

    suspend fun getEmptyConversations(): List<ConversationEntity> {
        return db.conversationDao().getAllConversations().filter { it.numberOfMessages == 0 }
    }

    suspend fun deleteConversations(conversations: List<ConversationEntity>) {
        db.conversationDao().deleteConversations(conversations)
    }

    suspend fun initializeConversations(context: Context) {
        withContext(Dispatchers.IO) {
            // 1. Charger totues les conversations existantes
            val existingConversations = db.conversationDao().getAllConversations()

            // 2. Charger les SMS du téléphone
            val smsMap = loadSmsFromDevice(context)

            // 3. Synchroniser avec la base
            smsMap.forEach { (address, messages) ->
                val normalizedPhone = normalizePhoneNumber(address)

                // Trouver ou créer la conversation
                var conversation = existingConversations.find { it.phoneNumber == normalizedPhone }
                if (conversation == null) {
                    val (name, photo) = lookupContact(context, address)
                    conversation = ConversationEntity(
                        id = normalizedPhone.hashCode().toLong(),
                        phoneNumber = normalizedPhone,
                        fullName = name ?: normalizedPhone,
                        lastMessage = messages.firstOrNull()?.body ?: "",
                        lastDate = messages.firstOrNull()?.date ?: System.currentTimeMillis(),
                        numberOfMessages = messages.size,
                        photoUri = photo
                    )

                    db.conversationDao().insertConversations(listOf(conversation))
                } else {
                    // Mettre à jour si nécessaire
                    val lastMessage = messages.maxByOrNull { it.date }
                    if (lastMessage != null && conversation.lastDate < lastMessage.date) {
                        conversation = conversation.copy(
                            lastMessage = lastMessage.body,
                            lastDate = lastMessage.date,
                            numberOfMessages = conversation.numberOfMessages + messages.size
                        )
                        db.conversationDao().updateConversation(conversation)
                    }
                }

                // 4. Insérer les messages dans la base
                val messageEntities = messages.map { sms ->
                    MessageEntity(
                        id = sms.id,
                        conversationId = conversation.id,
                        address = sms.address,
                        body = sms.body,
                        date = sms.date,
                        isMe = sms.type == 2, // 2 = envoyé, 1 = reçu
                        type = "sms",
                        status = if (sms.type == 2) "SENT" else "DELIVERED"
                    )
                }
                db.messageDao().insertMessages(messageEntities)
            }
        }
    }

    private fun loadSmsFromDevice(context: Context): Map<String, List<SmsData>> {
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.STATUS,
            Telephony.Sms.DATE_SENT,
        )

        val smsMap = mutableMapOf<String, MutableList<SmsData>>()

        context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val address = cursor.getString(addressIndex) ?: "Unknown"
                val body = cursor.getString(bodyIndex) ?: ""
                val date = cursor.getLong(dateIndex)
                val type = cursor.getInt(typeIndex)

                val smsData = SmsData(id, address, body, date, type)
                smsMap.getOrPut(address) { mutableListOf() }.add(smsData)
            }
        }

        return smsMap
    }

    private data class SmsData(
        val id: Long,
        val address: String,
        val body: String,
        val date: Long,
        val type: Int // 1=received, 2=sent
    )


    suspend fun getArchivedConversations(): List<ConversationEntity> {
        return db.conversationDao().getArchivedConversations()
    }

    suspend fun getPinnedConversations(): List<ConversationEntity> {
        return db.conversationDao().getAllConversations().filter { it.isPinned }
    }
    suspend fun getBlockedConversations(): List<ConversationEntity> {
        return db.conversationDao().getAllConversations().filter { it.isBlocked }
    }

    suspend fun archiveConversation(id: Long) {
        db.conversationDao().updateConversationArchived(id, true)
    }

    suspend fun unarchiveConversation(id: Long) {
        db.conversationDao().updateConversationArchived(id, false)
    }

    fun deleteConversation(id: Long) {
        db.conversationDao().deleteConversationById(id)
    }

    suspend fun getMutedConversations(): List<ConversationEntity> {
        return db.conversationDao().getAllConversations().filter { it.isMuted }
    }

    suspend fun blockConversation(id: Long) {
        db.conversationDao().updateConversationBlocked(id, true)
    }

    suspend fun unblockConversation(id: Long) {
        db.conversationDao().updateConversationBlocked(id, false)
    }

    suspend fun pinConversation(id: Long) {
        db.conversationDao().updateConversationPinned(id, true)
    }

    suspend fun unpinConversation(id: Long) {
        db.conversationDao().updateConversationPinned(id, false)
    }

    suspend fun muteConversation(id: Long) {
        db.conversationDao().updateConversationMuted(id, true)
    }

    suspend fun unmuteConversation(id: Long) {
        db.conversationDao().updateConversationMuted(id, false)
    }

}
