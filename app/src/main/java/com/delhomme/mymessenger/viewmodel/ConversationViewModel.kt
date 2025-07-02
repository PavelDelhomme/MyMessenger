package com.delhomme.mymessenger.viewmodel
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.delhomme.mymessenger.data.local.ConversationEntity
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

    suspend fun cleanEmptyConversations() {
        val emptyConvs = repo.getEmptyConversations()
        repo.deleteConversations(emptyConvs)
    }

    suspend fun getArchivedConversations(): List<ConversationEntity> {
        return repo.getArchivedConversations()
    }

    suspend fun getBlockedConversations(): List<ConversationEntity> {
        return repo.getBlockedConversations()
    }

    suspend fun getPinnedConversations(): List<ConversationEntity> {
        return repo.getPinnedConversations()
    }

    suspend fun getMutedConversations(): List<ConversationEntity> {
        return repo.getMutedConversations()
    }

    suspend fun archiveConversation(id: Long) {
        repo.archiveConversation(id)
    }

    suspend fun unarchiveConversation(id: Long) {
        repo.unarchiveConversation(id)
    }

    suspend fun blockConversation(id: Long) {
        repo.blockConversation(id)
    }

    suspend fun unblockConversation(id: Long) {
        repo.unblockConversation(id)
    }

    suspend fun pinConversation(id: Long) {
        repo.pinConversation(id)
    }

    suspend fun unpinConversation(id: Long) {
        repo.unpinConversation(id)
    }

    suspend fun muteConversation(id: Long) {
        repo.muteConversation(id)
    }

    suspend fun unmuteConversation(id: Long) {
        repo.unmuteConversation(id)
    }

    fun deleteConversation(id: Long) {
        repo.deleteConversation(id)
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
                ConversationAction.Call -> {
                    val conversation = repo.getConversationById(id)
                    conversation?.phoneNumber?.let { phoneNumber ->
                        context?.let { ctx ->
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${phoneNumber}")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            ctx.startActivity(intent)
                        }
                    }
                }
                else -> {/* Ne rien faire */}
            }
        }
    }
}