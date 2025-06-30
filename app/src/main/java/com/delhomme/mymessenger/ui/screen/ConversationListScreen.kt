package com.delhomme.mymessenger.ui.screen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.delhomme.mymessenger.ui.components.ConversationRow
import com.delhomme.mymessenger.viewmodel.ConversationViewModel



@Composable
fun ConversationListScreen(
    modifier: Modifier = Modifier,
    onConversationClick: (Long) -> Unit,
    viewModel: ConversationViewModel = hiltViewModel(),
) {
    val conversations = viewModel.conversations.collectAsLazyPagingItems()
    var query by rememberSaveable { mutableStateOf("") }

    Column {
        TextField(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth(),
            value = query,
            onValueChange = {
                query = it
                viewModel.updateQuery(it)
            },
            label = { Text("Rechercher") }
        )

        LazyColumn {
            items(
                count = conversations.itemCount,
                key = { index -> conversations[index]?.id ?: index }
            ) { index ->
                conversations[index]?.let { conv ->
                    ConversationRow(conversation = conv) {
                        onConversationClick(conv.id)
                    }
                }
            }
        }
    }
}