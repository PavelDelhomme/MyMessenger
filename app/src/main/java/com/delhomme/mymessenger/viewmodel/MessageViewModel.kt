package com.delhomme.mymessenger.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepo: MessageRepository
) : ViewModel() {
    fun getMessages(conversationId: Long): Flow<PagingData<MessageEntity>> {
        return Pager(PagingConfig(pageSize = 50)) {
            messageRepo.getPagedMessages(conversationId)
        }.flow.cachedIn(viewModelScope)
    }
}
