package com.delhomme.mymessenger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.data.repository.MessageRepository
import com.delhomme.mymessenger.data.repository.SmsSender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepo: MessageRepository,
    private val smsSender: SmsSender,
) : ViewModel() {
    fun getMessages(conversationId: Long): Flow<PagingData<MessageEntity>> {
        return Pager(PagingConfig(pageSize = 50)) {
            messageRepo.getPagedMessages(conversationId)
        }.flow.cachedIn(viewModelScope)
    }

    fun sendMessage(conversationId: Long, text: String) {
        viewModelScope.launch {
            // Créer le message local
            val message = MessageEntity(
                id = System.currentTimeMillis(),
                conversationId = conversationId,
                address = "Me",
                body = text,
                date = System.currentTimeMillis(),
                isMe = true,
                type = "sms",
                status = "SENDING"
            )

            // Insérer dans la base
            messageRepo.insertMessages(listOf(message))

            try {
                // Envoyer le SMS (remplacer par le vrai numéro)
                smsSender.sendSms("DESTINATAIRE", text)
                // Mettre à jour le statut
                messageRepo.updateMessageStatus(message.id, "SENT")
            } catch (e: Exception) {
                // En cas d'erreur
                messageRepo.updateMessageStatus(message.id, "FAILED")
            }
        }
    }
}
