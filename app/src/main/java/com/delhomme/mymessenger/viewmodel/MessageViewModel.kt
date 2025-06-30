package com.delhomme.mymessenger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.delhomme.mymessenger.data.repository.MessageRepository


class MessageViewModel(
    private val messageRepo: MessageRepository
) : ViewModel() {
    fun getMessages(conversationId: Long) =
        messageRepo.getPagedMessages(conversationId).cachedIn(viewModelScope)
}