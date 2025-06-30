package com.delhomme.mymessenger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.data.repository.MessageRepository
import kotlinx.coroutines.launch

class MessageViewModel(
    private val messageRepo: MessageRepository
) : ViewModel() {
    val messages = messageRepo.getPagedMessages(0L).cachedIn(viewModelScope)

    fun sendMessage(address: String, message: String) {
        viewModelScope.launch {
            val newMessage = MessageEntity(
                id = System.currentTimeMillis(),
                conversationId = 0L,
                address = address,
                body = message,
                date = System.currentTimeMillis(),
                isMe = true,
                type = "sms"
            )
            messageRepo.insertMessages(listOf(newMessage))
        }
    }
}
