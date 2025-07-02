package com.delhomme.mymessenger.ui.screen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.delhomme.mymessenger.ui.components.ConversationRow
import com.delhomme.mymessenger.ui.components.DrawerComponent
import com.delhomme.mymessenger.ui.components.SearchBar
import com.delhomme.mymessenger.viewmodel.ConversationViewModel
import com.delhomme.mymessenger.R
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.ui.components.ConversationAction
import kotlinx.coroutines.coroutineScope


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    onConversationClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onMenuClick: () -> Unit, // Nouveau callback pour le menu
    modifier: Modifier = Modifier,
    viewModel: ConversationViewModel = hiltViewModel(),
    navController: NavController
) {
    val conversations = viewModel.conversations.collectAsLazyPagingItems()
    var query by rememberSaveable { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var conversationToDelete by remember { mutableStateOf<ConversationEntity?>(null) }
    var conversationToArchive by remember { mutableStateOf<ConversationEntity?>(null) }



    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmer la suppression") },
            text = { Text("Voulez-vous vraiment supprimer cette conversation ?") },
            confirmButton = {
                Button(onClick = {
                    conversationToDelete?.id?.let { id ->
                        viewModel.handleAction(ConversationAction.Delete, id)
                    }
                    showDeleteDialog = false
                }) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, "Ajouter une conversation")
            }
        },
        topBar = {
            if (isSearchExpanded) {
                SearchBar(
                    query = query,
                    onQueryChange = {
                        query = it
                        viewModel.updateQuery(it)
                    },
                    placeholder = "Rechercher des conversations...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    onClose = { isSearchExpanded = false },
                    /*drawerComponent = DrawerComponent(
                        navController = navController,
                        onClose = { isSearchExpanded = false }
                    )*/
                )
            } else {
                TopAppBar(
                    title = { Text("Conversations") },
                    actions = {
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(Icons.Default.Search, "Rechercher")
                        }
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                items(
                    count = conversations.itemCount,
                    key = { index -> conversations[index]?.id ?: index }
                ) { index ->
                    conversations[index]?.let { conv ->
                        ConversationRow(
                            conversation = conv,
                            onAction = { action ->
                                when (action) {
                                    ConversationAction.Delete -> {
                                        conversationToDelete = conv
                                        showDeleteDialog = true
                                    }
                                    else -> viewModel.handleAction(action, conv.id)
                                }
                            },
                            onClick = { onConversationClick(conv.id) }
                        )
                    }
                }
            }
        }
    }
}
