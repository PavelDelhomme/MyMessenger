package com.delhomme.mymessenger.ui.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.delhomme.mymessenger.ui.components.ConversationRow
import com.delhomme.mymessenger.viewmodel.ConversationViewModel


@Composable
fun ConversationListScreen(viewModel: ConversationViewModel, onConversationClick: (Long) -> Unit) {
    val conversations = viewModel.conversations.collectAsLazyPagingItems()
    LazyColumn {
        items(conversations) { conv ->
            conv?.let {
                ConversationRow(it, onClick = { onConversationClick(it.id) })
            }
        }
    }
}