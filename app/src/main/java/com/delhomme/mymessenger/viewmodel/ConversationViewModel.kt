package com.delhomme.mymessenger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.data.repository.ConversationRepository
import kotlinx.coroutines.launch

class ConversationViewModel(
    private val conversationRepo: ConversationRepository
) : ViewModel() {
    val conversations = conversationRepo.getPagedConversations().cachedIn(viewModelScope)

    // Initiation du chargement des données
    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            conversationRepo.insertConversations(
                listOf(
                    ConversationEntity(
                        id = 1L,
                        address = "+33612345678",
                        lastMessage = "Test message",
                        lastDate = System.currentTimeMillis()
                    )
                )
            )
        }
    }
}
