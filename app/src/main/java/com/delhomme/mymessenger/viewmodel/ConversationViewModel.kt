package com.delhomme.mymessenger.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.data.repository.ConversationRepository
import com.delhomme.mymessenger.ui.components.ConversationAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val repo: ConversationRepository
) : ViewModel() {

    private val _search = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _search

    val selectedConversations = mutableStateListOf<Long>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val conversations: Flow<PagingData<ConversationEntity>> = _search
        .flatMapLatest { q ->
            Pager(PagingConfig(pageSize = 20)) {
                repo.pagedConversations(q)
            }.flow
        }
        .cachedIn(viewModelScope)

    fun updateQuery(q: String) { _search.value = q }

    suspend fun createConversationIfNeeded(phoneNumber: String, fullName: String) {
        if (repo.getConversationByPhone(phoneNumber) == null) {
            val newId = phoneNumber.hashCode().toLong()
            val newConversation = ConversationEntity(
                id = newId,
                phoneNumber = phoneNumber,
                fullName = fullName
            )
            repo.insertConversation(newConversation)
        }
    }

    fun handleAction(action: ConversationAction, id: Long, context: Context? = null) {
        viewModelScope.launch {
            when (action) {
                ConversationAction.Archive -> archiveConversation(id)
                ConversationAction.Unarchive -> unarchiveConversation(id)
                ConversationAction.Pin -> pinConversation(id)
                ConversationAction.Unpin -> unpinConversation(id)
                ConversationAction.Mute -> muteConversation(id)
                ConversationAction.Unmute -> unmuteConversation(id)
                ConversationAction.Block -> blockConversation(id)
                ConversationAction.Unblock -> unblockConversation(id)
                ConversationAction.Delete -> deleteConversation(id)
                ConversationAction.MarkAsRead -> markAsRead(id)
                ConversationAction.MarkAsUnread -> markAsUnread(id)
                ConversationAction.Call -> {
                    val conversation = repo.getConversationById(id)
                    conversation?.phoneNumber?.let { phoneNumber ->
                        context?.let { ctx ->
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phoneNumber")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            ctx.startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    private suspend fun archiveConversation(id: Long) {
        val conversation = repo.getConversationById(id)
        conversation?.let {
            val updated = it.copy(isArchived = true)
            repo.updateConversation(updated)
        }
    }

    private suspend fun unarchiveConversation(id: Long) {
        val conversation = repo.getConversationById(id)
        conversation?.let {
            val updated = it.copy(isArchived = false)
            repo.updateConversation(updated)
        }
    }

    private suspend fun pinConversation(id: Long) {
        val conversation = repo.getConversationById(id)
        conversation?.let {
            val updated = it.copy(isPinned = true)
            repo.updateConversation(updated)
        }
    }

    private suspend fun unpinConversation(id: Long) {
        val conversation = repo.getConversationById(id)
        conversation?.let {
            val updated = it.copy(isPinned = false)
            repo.updateConversation(updated)
        }
    }

    private suspend fun muteConversation(id: Long) {
        val conversation = repo.getConversationById(id)
        conversation?.let {
            val updated = it.copy(isMuted = true)
            repo.updateConversation(updated)
        }
    }

    private suspend fun unmuteConversation(id: Long) {
        val conversation = repo.getConversationById(id)
        conversation?.let {
            val updated = it.copy(isMuted = false)
            repo.updateConversation(updated)
        }
    }

    private suspend fun blockConversation(id: Long) {
        val conversation = repo.getConversationById(id)
        conversation?.let {
            val updated = it.copy(isBlocked = true)
            repo.updateConversation(updated)
        }
    }

    private suspend fun unblockConversation(id: Long) {
        val conversation = repo.getConversationById(id)
        conversation?.let {
            val updated = it.copy(isBlocked = false)
            repo.updateConversation(updated)
        }
    }

    private suspend fun deleteConversation(id: Long) {
        val conversation = repo.getConversationById(id)
        conversation?.let {
            val updated = it.copy(isDeleted = true)
            repo.updateConversation(updated)
        }
    }

    private suspend fun markAsRead(id: Long) {
        val conversation = repo.getConversationById(id)
        conversation?.let {
            val updated = it.copy(unreadCount = 0)
            repo.updateConversation(updated)
        }
    }

    private suspend fun markAsUnread(id: Long) {
        val conversation = repo.getConversationById(id)
        conversation?.let {
            val updated = it.copy(unreadCount = if (it.unreadCount == 0) 1 else it.unreadCount)
            repo.updateConversation(updated)
        }
    }

    fun getMessages(conversationId: Long): Flow<PagingData<MessageEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 30, enablePlaceholders = false)
        ) {
            repo.getMessagesByConversationPaging(conversationId)
        }.flow.cachedIn(viewModelScope)
    }

    fun cleanEmptyConversations() {
        viewModelScope.launch {
            val emptyConvs = repo.getEmptyConversations()
            repo.cleanEmptyConversation(emptyConvs)
        }
    }

    fun initializeConversations(context: Context) {
        viewModelScope.launch {
            repo.initializeConversations(context)
        }
    }
}