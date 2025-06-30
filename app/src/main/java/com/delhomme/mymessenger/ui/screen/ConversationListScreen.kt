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
import androidx.paging.compose.collectAsLazyPagingItems
import com.delhomme.mymessenger.ui.components.ConversationRow
import com.delhomme.mymessenger.viewmodel.ConversationViewModel


@Composable
fun ConversationListScreen(viewModel: ConversationViewModel, onConversationClick: (Long) -> Unit) {
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
            items(viewModel.conversations) { conv ->
                conv?.let {
                    ConversationRow(it, onClick = { onConversationClick(it.id) })
                }
            }
        }
    }
}
