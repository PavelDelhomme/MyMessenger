package com.delhomme.mymessenger.ui.screen
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import com.delhomme.mymessenger.ui.components.ConversationRow
import com.delhomme.mymessenger.ui.components.DrawerComponent
import com.delhomme.mymessenger.ui.components.SearchBar
import com.delhomme.mymessenger.viewmodel.ConversationViewModel
import com.delhomme.mymessenger.R
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.ui.components.ConversationAction
import com.delhomme.mymessenger.ui.screen.permissions.WithPermission
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
    var showImportDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current


    // Dialog d'import
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Importer l'historique") },
            text = { Text("Voulez-vous importer vos SMS existants ?") },
            confirmButton = {
                WithPermission(
                    permission = Manifest.permission.READ_SMS,
                    onAction = {
                        viewModel.initializeConversations(context)
                        Toast.makeText(context, "Import lancé", Toast.LENGTH_SHORT).show()
                        showImportDialog = false
                    },
                    onPermissionDenied = {
                        Toast.makeText(context, "Permission nécessaire pour l'import", Toast.LENGTH_SHORT).show()
                        showImportDialog = false
                    }
                ) { requestPermission ->
                    Button(onClick = { requestPermission() }) {
                        Text("Importer")
                    }
                }
            },
            dismissButton = {
                Button(onClick = { showImportDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog de suppression
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
                )
            } else {
                TopAppBar(
                    title = { Text("Conversations") },
                    actions = {
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(Icons.Default.Search, "Rechercher")
                        }
                        IconButton(onClick = { showImportDialog = true }) {
                            Icon(Icons.Default.Refresh, "Importer SMS")
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, "Ajouter une conversation")
            }
        }

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (conversations.itemCount == 0) {
                // État vide
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Aucune conversation",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Appuyez sur + pour commencer une nouvelle conversation",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                // Liste des conversations
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
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
                                        else -> viewModel.handleAction(action, conv.id, context)
                                    }
                                },
                                onClick = { onConversationClick(conv.id) },
                                onLongClick = {
                                    if (viewModel.selectedConversations.contains(conv.id)) {
                                        viewModel.selectedConversations.remove(conv.id)
                                    } else {
                                        viewModel.selectedConversations.add(conv.id)
                                    }
                                },
                                isSelected = viewModel.selectedConversations.contains(conv.id)
                            )
                        }
                    }
                }
            }
        }
    }
/*    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
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
                            onClick = { onConversationClick(conv.id) },
                            onLongClick = { viewModel.selectedConversations.add(conv.id) },
                            isSelected = viewModel.selectedConversations.contains(conv.id),
                        )
                    }
                }
            }
        }
    }*/
}
