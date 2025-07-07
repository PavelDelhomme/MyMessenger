package com.delhomme.mymessenger.data.repository

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import androidx.paging.PagingSource
import com.delhomme.mymessenger.data.local.AppDatabase
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.utils.lookupContact
import com.delhomme.mymessenger.utils.normalizePhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConversationRepository(private val db: AppDatabase) {
    fun getPagedConversations(): PagingSource<Int, ConversationEntity> {
        return db.conversationDao().getAllConversationsPaging()
    }

    fun getMessagesByConversationPaging(conversationId: Long) = db.messageDao().getMessagesByConversationPaging(conversationId)

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

    suspend fun initializeConversations(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("CONVERSATION_REPO", "Starting conversation initialization")

                val existingConversations = db.conversationDao().getAllConversations()
                Log.d("CONVERSATION_REPO", "Found ${existingConversations.size} existing conversations")

                // Charger les SMS
                Log.d("CONVERSATION_REPO", "Loading SMS messages...")
                val smsMap = loadSmsFromDevice(context)
                Log.d("CONVERSATION_REPO", "Loaded SMS from ${smsMap.size} addresses")

                // Charger les MMS avec gestion d'erreur
                Log.d("CONVERSATION_REPO", "Loading MMS messages...")
                val mmsMap = try {
                    loadMmsFromDevice(context)
                } catch (e: Exception) {
                    Log.e("CONVERSATION_REPO", "Error loading MMS, continuing without MMS", e)
                    emptyMap<String, List<MmsData>>()
                }
                Log.d("CONVERSATION_REPO", "Loaded MMS from ${mmsMap.size} addresses")

                // Merge SMS et MMS par numéro
                val allAddresses = (smsMap.keys + mmsMap.keys).toSet()
                Log.d("CONVERSATION_REPO", "Processing ${allAddresses.size} unique addresses")

                allAddresses.forEach { address ->
                    try {
                        val normalizedPhone = normalizePhoneNumber(address)
                        var conversation = existingConversations.find { it.phoneNumber == normalizedPhone }

                        // Récupérer tous les messages (SMS + MMS)
                        val smsMessages = smsMap[address] ?: emptyList()
                        val mmsMessages = mmsMap[address] ?: emptyList()

                        val allMessages = (smsMessages.map {
                            MessageEntity(
                                id = it.id,
                                conversationId = normalizedPhone.hashCode().toLong(),
                                address = it.address,
                                body = it.body,
                                date = it.date,
                                isMe = it.type == 2,
                                type = "sms",
                                status = if (it.type == 2) "SENT" else "DELIVERED"
                            )
                        } + mmsMessages.map {
                            MessageEntity(
                                id = it.id,
                                conversationId = normalizedPhone.hashCode().toLong(),
                                address = it.address,
                                body = it.body,
                                date = it.date,
                                isMe = it.box == 2, // 2 = sent
                                type = "mms",
                                status = if (it.box == 2) "SENT" else "DELIVERED"
                            )
                        }).sortedBy { it.date }

                        if (conversation == null) {
                            val (name, photo) = lookupContact(context, address)
                            conversation = ConversationEntity(
                                id = normalizedPhone.hashCode().toLong(),
                                phoneNumber = normalizedPhone,
                                fullName = name ?: normalizedPhone,
                                lastMessage = allMessages.lastOrNull()?.body ?: "",
                                lastDate = allMessages.lastOrNull()?.date ?: System.currentTimeMillis(),
                                numberOfMessages = allMessages.size,
                                photoUri = photo
                            )
                            db.conversationDao().insertConversations(listOf(conversation))
                        } else {
                            val lastMessage = allMessages.maxByOrNull { it.date }
                            if (lastMessage != null && conversation.lastDate < lastMessage.date) {
                                conversation = conversation.copy(
                                    lastMessage = lastMessage.body,
                                    lastDate = lastMessage.date,
                                    numberOfMessages = allMessages.size
                                )
                                db.conversationDao().updateConversation(conversation)
                            }
                        }

                        if (allMessages.isNotEmpty()) {
                            db.messageDao().insertMessages(allMessages)
                        }

                    } catch (e: Exception) {
                        Log.e("CONVERSATION_REPO", "Error processing address $address", e)
                        // Continue avec l'adresse suivante
                    }
                }

                Log.d("CONVERSATION_REPO", "Conversation initialization completed successfully")

            } catch (e: Exception) {
                Log.e("CONVERSATION_REPO", "Fatal error during conversation initialization", e)
                // Relancer l'exception seulement si c'est critique
                if (e is SecurityException) {
                    throw e
                }
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

        try {
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
        } catch (e: Exception) {
            Log.e("CONVERSATION_REPO", "Error loading SMS", e)
        }

        return smsMap
    }

    private fun loadMmsFromDevice(context: Context): Map<String, List<MmsData>> {
        val mmsMap = mutableMapOf<String, MutableList<MmsData>>()
        val uri = Telephony.Mms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Mms._ID,
            Telephony.Mms.DATE,
            Telephony.Mms.MESSAGE_BOX
        )

        try {
            context.contentResolver.query(uri, projection, null, null, "${Telephony.Mms.DATE} DESC")?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(Telephony.Mms._ID)
                val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Mms.DATE)
                val boxIndex = cursor.getColumnIndexOrThrow(Telephony.Mms.MESSAGE_BOX)

                while (cursor.moveToNext()) {
                    try {
                        val id = cursor.getLong(idIndex)
                        val date = cursor.getLong(dateIndex) * 1000 // MMS date is in seconds
                        val box = cursor.getInt(boxIndex) // 1 = inbox, 2 = sent

                        // Récupérer l'adresse (expéditeur ou destinataire)
                        val addrUri = Uri.parse("content://mms/$id/addr")
                        val addrCursor = context.contentResolver.query(addrUri, arrayOf("address", "type"), null, null, null)
                        var address: String? = null
                        addrCursor?.use {
                            while (it.moveToNext()) {
                                val type = it.getInt(it.getColumnIndexOrThrow("type"))
                                if (type == 137 || type == 151) { // 137 = sender, 151 = recipient
                                    address = it.getString(it.getColumnIndexOrThrow("address"))
                                    if (address != null && address != "insert-address-token") break
                                }
                            }
                        }

                        // Récupérer le texte du MMS de manière sécurisée
                        var body = ""
                        try {
                            val partUri = Uri.parse("content://mms/$id/part")
                            val partCursor = context.contentResolver.query(partUri, null, null, null, null)
                            partCursor?.use {
                                while (it.moveToNext()) {
                                    val ct = it.getString(it.getColumnIndexOrThrow("ct"))
                                    if (ct == "text/plain") {
                                        val partId = it.getString(it.getColumnIndexOrThrow("_id"))
                                        try {
                                            val inputStream = context.contentResolver.openInputStream(Uri.parse("content://mms/part/$partId"))
                                            body = inputStream?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
                                            break
                                        } catch (e: Exception) {
                                            Log.w("CONVERSATION_REPO", "Could not read MMS part content for ID $partId", e)
                                            // Continue avec le texte vide
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("CONVERSATION_REPO", "Could not read MMS parts for ID $id", e)
                            body = "[MMS]" // Texte par défaut
                        }

                        if (!address.isNullOrBlank()) {
                            val mmsData = MmsData(id, address!!, body, date, box)
                            mmsMap.getOrPut(address!!) { mutableListOf() }.add(mmsData)
                        }

                    } catch (e: Exception) {
                        Log.w("CONVERSATION_REPO", "Error processing individual MMS record", e)
                        // Continue avec le message suivant
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CONVERSATION_REPO", "Error loading MMS data", e)
            throw e
        }

        return mmsMap
    }

    suspend fun cleanEmptyConversation(conversations: List<ConversationEntity>) {
        db.conversationDao().deleteConversations(conversations)
    }

    private data class SmsData(
        val id: Long,
        val address: String,
        val body: String,
        val date: Long,
        val type: Int // 1=received, 2=sent
    )

    private data class MmsData(
        val id: Long,
        val address: String,
        val body: String,
        val date: Long,
        val box: Int // 1=inbox, 2=sent
    )
}