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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
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
}