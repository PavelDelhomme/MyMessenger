package com.delhomme.mymessenger.ui.screen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.delhomme.mymessenger.ui.components.ConversationRow
import com.delhomme.mymessenger.viewmodel.ConversationViewModel

@Composable
fun ConversationListScreen(
    onConversationClick: (Long) -> Unit,
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val conversations = viewModel.conversations.collectAsLazyPagingItems()
    Column {
        TextField(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            value = "",
            onValueChange = { /* Implémente la recherche */ },
            label = { Text("Rechercher") }
        )

        LazyColumn {
            items(conversations.itemCount) { index ->
                conversations[index]?.let { conv ->
                    ConversationRow(
                        conversation = conv,
                        onClick = { onConversationClick(conv.id) }
                    )
                }
            }
        }
    }
}