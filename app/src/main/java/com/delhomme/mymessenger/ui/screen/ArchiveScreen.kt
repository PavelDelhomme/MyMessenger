package com.delhomme.mymessenger.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.ui.components.ConversationAction
import com.delhomme.mymessenger.ui.components.ConversationRow
import com.delhomme.mymessenger.viewmodel.ConversationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(navController: NavController) {
    val viewModel: ConversationViewModel = hiltViewModel()
    var archivedConversations by remember { mutableStateOf(emptyList<ConversationEntity>()) }

    LaunchedEffect(Unit) {
        archivedConversations = viewModel.getArchivedConversations()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Conversations archivées") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(archivedConversations.size) { conv ->
                ConversationRow(
                    conversation = conv,
                    onAction = { action ->
                        when (action) {
                            ConversationAction.Unarchive -> {
                                viewModel.unarchiveConversation(conv.id)
                                // Rafraîchir la liste
                                archivedConversations = archivedConversations.filter { it.id != conv.id }
                            }
                            // Ajouter d'autres actions au besoin
                            else -> {}
                        }
                    },
                    onClick = { /* ... */ }
                )
            }
        }
    }
}