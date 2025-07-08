package com.delhomme.mymessenger.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.data.repository.ConversationRepository
import com.delhomme.mymessenger.data.repository.MessageRepository
import com.delhomme.mymessenger.data.repository.SmsSender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val repo: MessageRepository,
    private val smsSender: SmsSender,
    private val conversationRepo: ConversationRepository
) : ViewModel() {
    fun getMessages(conversationId: Long): Flow<PagingData<MessageEntity>> {
        println("getMessages() appelé, retourne un Flow<PagingData<MessageEntity>>")
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false
            )
        ) {
            repo.getMessagesByConversationPaging(conversationId)
        }.flow.cachedIn(viewModelScope)
    }

    // Méthode pour envoyer un SMS simple
    fun sendMessage(conversationId: Long, text: String, phoneNumber: String, replyToId: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Créer le message local avec replyToId
                val message = MessageEntity(
                    id = System.currentTimeMillis(),
                    conversationId = conversationId,
                    address = "Me",
                    body = text,
                    date = System.currentTimeMillis(),
                    isMe = true,
                    type = "sms",
                    status = "SENDING",
                    replyToId = replyToId
                )

                // Insérer dans la base
                repo.insertMessages(listOf(message))

                // Mettre à jour la conversation
                conversationRepo.updateConversationLastMessage(
                    conversationId = conversationId,
                    lastMessage = text,
                    lastDate = System.currentTimeMillis()
                )

                // Envoi réel du SMS (en arrière-plan)
                smsSender.sendSms(phoneNumber, text)

                // Mettre à jour le statut à "SENT" si l'envoi réussit
                repo.updateMessageStatus(message.id, "SENT")

            } catch (e: Exception) {
                // En cas d'erreur, marquer le message comme "FAILED"
                val failedMessage = MessageEntity(
                    id = System.currentTimeMillis(),
                    conversationId = conversationId,
                    address = "Me",
                    body = text,
                    date = System.currentTimeMillis(),
                    isMe = true,
                    type = "sms",
                    status = "FAILED",
                    replyToId = replyToId
                )
                repo.insertMessages(listOf(failedMessage))
            }
        }
    }


    // Méthode pour envoyer un MMS avec média
    fun sendMmsMessage(
        conversationId: Long,
        text: String,
        phoneNumber: String,
        mediaUri: Uri,
        replyToId: Long? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Créer le message local
                val message = MessageEntity(
                    id = System.currentTimeMillis(),
                    conversationId = conversationId,
                    address = "Me",
                    body = text,
                    date = System.currentTimeMillis(),
                    isMe = true,
                    type = "mms",
                    status = "SENDING",
                    replyToId = replyToId
                )

                // Insérer dans la base
                repo.insertMessages(listOf(message))

                // Mettre à jour la conversation
                conversationRepo.updateConversationLastMessage(
                    conversationId = conversationId,
                    lastMessage = text,
                    lastDate = System.currentTimeMillis()
                )

                // Envoi réel du MMS
                smsSender.sendMms(phoneNumber, text, mediaUri)

                // Mettre à jour le statut
                repo.updateMessageStatus(message.id, "SENT")

            } catch (e: Exception) {
                // En cas d'erreur, marquer comme "FAILED"
                val failedMessage = MessageEntity(
                    id = System.currentTimeMillis(),
                    conversationId = conversationId,
                    address = "Me",
                    body = text,
                    date = System.currentTimeMillis(),
                    isMe = true,
                    type = "mms",
                    status = "FAILED",
                    replyToId = replyToId
                )
                repo.insertMessages(listOf(failedMessage))
            }
        }
    }

    suspend fun getConversation(conversationId: Long): ConversationEntity? {
        return conversationRepo.getConversationById(conversationId)
    }

    suspend fun getMessageById(messageId: Long): MessageEntity? {
        return repo.getMessageById(messageId)
    }

    fun copyMessagesToClipboard(ids: List<Long>, context: Context) {
        viewModelScope.launch {
            val text = repo.getMessagesText(ids).joinToString("\n")
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Messages", text)
            clipboard.setPrimaryClip(clip)
        }
    }

    fun logMessagesForConversation(conversationId: Long) {
        viewModelScope.launch {
            val messages = repo.getAllMessagesForConversation(conversationId)
            println("DEBUG: Nombre de messages pour convId=$conversationId : ${messages.size}")
            messages.forEach { println("DEBUG: Message = $it") }
        }
    }
    fun logAllMessages() {
        viewModelScope.launch {
            val messages = repo.getAllMessages()
            println("==== TOUS LES MESSAGES EN BASE ====")
            messages.forEach { println(it) }
        }
    }

}
