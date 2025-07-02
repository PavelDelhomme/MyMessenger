package com.delhomme.mymessenger.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false
            )
        ) {
            repo.getMessagesByConversationPaging(conversationId)
        }.flow.cachedIn(viewModelScope)
    }

    fun sendMessage(conversationId: Long, text: String, phoneNumber: String, replyToId: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
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
                replyToId = replyToId // <- Ajout du replyToId
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
        }
    }

    suspend fun getConversation(conversationId: Long): ConversationEntity? {
        return conversationRepo.getConversationById(conversationId)
    }

    suspend fun getMessageById(messageId: Long): MessageEntity? {
        return repo.getMessageById(messageId)
    }

    fun deleteMessages(ids: List<Long>) = viewModelScope.launch {
        repo.deleteMessages(ids)
    }

    fun copyMessagesToClipboard(ids: List<Long>, context: Context) {
        viewModelScope.launch {
            val text = repo.getMessagesText(ids).joinToString("\n")
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Messages", text)
            clipboard.setPrimaryClip(clip)
        }
    }
}
