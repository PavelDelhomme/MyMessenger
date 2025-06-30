package com.delhomme.mymessenger.ui.screen
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.delhomme.mymessenger.ui.components.MessageBubble
import com.delhomme.mymessenger.viewmodel.MessageViewModel

@Composable
fun MessageListScreen(
    conversationId: Long,
    viewModel: MessageViewModel = hiltViewModel()
) {
    val messages = viewModel.getMessages(conversationId).collectAsLazyPagingItems()

    LazyColumn {
        items(messages.itemCount) { index ->
            messages[index]?.let { msg ->
                MessageBubble(message = msg)
            }
        }
    }
}
