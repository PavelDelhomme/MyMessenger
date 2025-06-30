package com.delhomme.mymessenger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.delhomme.mymessenger.data.repository.ConversationRepository


class ConversationViewModel(
    private val conversationRepo: ConversationRepository
) : ViewModel() {
    val conversations = conversationRepo.getPagedConversations().cachedIn(viewModelScope)
}