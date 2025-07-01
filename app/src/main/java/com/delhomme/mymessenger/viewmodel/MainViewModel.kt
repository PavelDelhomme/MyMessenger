package com.delhomme.mymessenger.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delhomme.mymessenger.data.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val conversationRepo: ConversationRepository
) : ViewModel() {
    fun initializeConversations(context: Context) {
        viewModelScope.launch {
            conversationRepo.initializeConversations(context)
        }
    }
}