package com.delhomme.mymessenger.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.data.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val conversationRepo: ConversationRepository
) : ViewModel() {
    val conversations: Flow<PagingData<ConversationEntity>> =
        Pager(PagingConfig(pageSize = 20)) {
            conversationRepo.getPagedConversations()
        }.flow.cachedIn(viewModelScope)
}