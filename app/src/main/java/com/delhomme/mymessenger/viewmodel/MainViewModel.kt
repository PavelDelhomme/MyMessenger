package com.delhomme.mymessenger.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delhomme.mymessenger.data.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val conversationRepo: ConversationRepository
) : ViewModel() {

    private val _initializationProgress = MutableStateFlow(0f)
    val initializationProgress: StateFlow<Float> = _initializationProgress

    private val _initializationMessage = MutableStateFlow("")
    val initializationMessage: StateFlow<String> = _initializationMessage

    private val _isInitializing = MutableStateFlow(false)
    val isInitializing: StateFlow<Boolean> = _isInitializing

    fun initializeConversations(context: Context) {
        viewModelScope.launch {
            _isInitializing.value = true

            conversationRepo.initializeConversations(
                context = context,
                onProgress = { progress, message ->
                    _initializationProgress.value = progress
                    _initializationMessage.value = message
                }
            )

            _isInitializing.value = false
        }
    }
}