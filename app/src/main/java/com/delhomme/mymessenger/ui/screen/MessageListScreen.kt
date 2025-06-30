package com.delhomme.mymessenger.ui.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.delhomme.mymessenger.ui.components.MessageBubble
import com.delhomme.mymessenger.viewmodel.MessageViewModel


@Composable
fun MessageListScreen(viewModel: MessageViewModel, conversationId: Long) {
    val messages = viewModel.getMessages(conversationId).collectAsLazyPagingItems()
    LazyColumn {
        items(messages) { msg ->
            msg?.let { MessageBubble(it) }
        }
    }
}