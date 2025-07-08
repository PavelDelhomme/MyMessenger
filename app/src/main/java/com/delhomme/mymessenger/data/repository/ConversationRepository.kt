package com.delhomme.mymessenger.data.repository

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
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
                val smsMap = loadSmsFromDevice(context)
                Log.d("CONVERSATION_REPO", "Loaded SMS from ${smsMap.size} addresses")

                // Charger les MMS avec la nouvelle méthode
                val mmsMap = loadMmsFromDeviceImproved(context)
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
                                isMe = it.box == 2,
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
                    }
                }

                Log.d("CONVERSATION_REPO", "Conversation initialization completed successfully")

            } catch (e: Exception) {
                Log.e("CONVERSATION_REPO", "Error during conversation initialization", e)
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
            Telephony.Sms.TYPE
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


    /**
     * Méthode améliorée pour charger les MMS - utilise l'approche des apps SMS professionnelles
     */
    private fun loadMmsFromDeviceImproved(context: Context): Map<String, List<MmsData>> {
        val mmsMap = mutableMapOf<String, MutableList<MmsData>>()

        try {
            // 1. Récupérer les MMS de base
            val mmsUri = Telephony.Mms.CONTENT_URI
            val mmsProjection = arrayOf(
                Telephony.Mms._ID,
                Telephony.Mms.DATE,
                Telephony.Mms.MESSAGE_BOX,
                Telephony.Mms.SUBJECT
            )

            context.contentResolver.query(mmsUri, mmsProjection, null, null, "${Telephony.Mms.DATE} DESC")?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(Telephony.Mms._ID)
                val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Mms.DATE)
                val boxIndex = cursor.getColumnIndexOrThrow(Telephony.Mms.MESSAGE_BOX)
                val subjectIndex = cursor.getColumnIndexOrThrow(Telephony.Mms.SUBJECT)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val date = cursor.getLong(dateIndex) * 1000
                    val box = cursor.getInt(boxIndex)
                    val subject = cursor.getString(subjectIndex) ?: ""

                    try {
                        // 2. Récupérer l'adresse de manière plus robuste
                        val address = getMmsAddress(context, id, box == 2)

                        if (!address.isNullOrBlank()) {
                            // 3. Récupérer le texte MMS de manière sécurisée
                            val text = getMmsText(context, id)

                            // 4. Construire le corps du message
                            val body = buildMmsBody(subject, text)

                            val mmsData = MmsData(id, address, body, date, box)
                            mmsMap.getOrPut(address) { mutableListOf() }.add(mmsData)

                            Log.d("CONVERSATION_REPO", "Successfully loaded MMS $id from $address")
                        }

                    } catch (e: Exception) {
                        Log.w("CONVERSATION_REPO", "Could not process MMS $id: ${e.message}")
                        // Continue avec le MMS suivant
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CONVERSATION_REPO", "Error loading MMS data", e)
        }

        return mmsMap
    }

    /**
     * Récupère l'adresse d'un MMS de manière plus robuste
     */
    private fun getMmsAddress(context: Context, mmsId: Long, isSent: Boolean): String? {
        try {
            val addrUri = Uri.parse("content://mms/$mmsId/addr")

            context.contentResolver.query(addrUri, arrayOf("address", "type"), null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val type = cursor.getInt(cursor.getColumnIndexOrThrow("type"))
                    val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))

                    // Types d'adresse MMS :
                    // 137 = FROM (expéditeur)
                    // 151 = TO (destinataire)
                    // 130 = BCC
                    // 129 = CC

                    val isValidAddress = when {
                        isSent && (type == 151) -> true  // Message envoyé, prendre le destinataire
                        !isSent && (type == 137) -> true // Message reçu, prendre l'expéditeur
                        else -> false
                    }

                    if (isValidAddress && !address.isNullOrBlank() && address != "insert-address-token") {
                        return address
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("CONVERSATION_REPO", "Error getting MMS address for $mmsId", e)
        }
        return null
    }


    /**
     * Récupère le texte d'un MMS sans utiliser openInputStream
     */
    private fun getMmsText(context: Context, mmsId: Long): String {
        try {
            val partUri = Uri.parse("content://mms/$mmsId/part")
            val partProjection = arrayOf("_id", "ct", "text")

            context.contentResolver.query(partUri, partProjection, null, null, null)?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow("_id")
                val ctIndex = cursor.getColumnIndexOrThrow("ct")
                val textIndex = cursor.getColumnIndexOrThrow("text")

                while (cursor.moveToNext()) {
                    val contentType = cursor.getString(ctIndex)

                    if (contentType == "text/plain") {
                        // Essayer d'abord la colonne 'text' directement
                        val text = cursor.getString(textIndex)
                        if (!text.isNullOrBlank()) {
                            return text
                        }

                        // Si pas de texte direct, essayer une approche alternative
                        val partId = cursor.getLong(idIndex)
                        val alternativeText = getMmsTextAlternative(context, partId)
                        if (!alternativeText.isNullOrBlank()) {
                            return alternativeText
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("CONVERSATION_REPO", "Error getting MMS text for $mmsId", e)
        }
        return ""
    }


    /**
     * Méthode alternative pour récupérer le texte MMS
     */
    private fun getMmsTextAlternative(context: Context, partId: Long): String? {
        return try {
            // Utiliser l'API Telephony si disponible (Android 10+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val partUri = Uri.parse("content://mms/part/$partId")
                context.contentResolver.query(partUri, arrayOf("text"), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getString(0)
                    } else null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("CONVERSATION_REPO", "Alternative MMS text method failed for part $partId", e)
            null
        }
    }

    /**
     * Construit le corps du message MMS à partir du sujet et du texte
     */
    private fun buildMmsBody(subject: String, text: String): String {
        return when {
            subject.isNotBlank() && text.isNotBlank() -> "$subject\n$text"
            subject.isNotBlank() -> subject
            text.isNotBlank() -> text
            else -> "[MMS]"
        }
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
