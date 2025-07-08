package com.delhomme.mymessenger.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import android.Manifest
import com.delhomme.mymessenger.ui.components.ConversationRow
import com.delhomme.mymessenger.ui.components.SearchBar
import com.delhomme.mymessenger.viewmodel.ConversationViewModel
import com.delhomme.mymessenger.viewmodel.MainViewModel
import com.delhomme.mymessenger.R
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.ui.components.ConversationAction
import com.delhomme.mymessenger.ui.screen.permissions.WithPermission
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    onConversationClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConversationViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    navController: NavController
) {
    val conversations = viewModel.conversations.collectAsLazyPagingItems()
    var query by rememberSaveable { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var conversationToDelete by remember { mutableStateOf<ConversationEntity?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingProgress by remember { mutableStateOf(0f) }
    var loadingMessage by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Dialog d'import avec loader
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showImportDialog = false },
            title = { Text("Importer l'historique") },
            text = {
                Column {
                    Text("Voulez-vous importer vos SMS existants ?")
                    if (isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { loadingProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = loadingMessage,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                if (!isLoading) {
                    WithPermission(
                        permission = Manifest.permission.READ_SMS,
                        onAction = {
                            isLoading = true
                            loadingMessage = "Initialisation..."
                            // Simuler la progression
                            LaunchedEffect(Unit) {
                                for (i in 0..100) {
                                    loadingProgress = i / 100f
                                    loadingMessage = when {
                                        i < 30 -> "Chargement des SMS... ($i%)"
                                        i < 60 -> "Chargement des MMS... ($i%)"
                                        i < 90 -> "Création des conversations... ($i%)"
                                        else -> "Finalisation... ($i%)"
                                    }
                                    delay(50) // Ajuster selon les besoins
                                }
                                mainViewModel.initializeConversations(context)
                                isLoading = false
                                showImportDialog = false
                                Toast.makeText(context, "Import terminé", Toast.LENGTH_SHORT).show()
                            }
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
                }
            },
            dismissButton = {
                if (!isLoading) {
                    Button(onClick = { showImportDialog = false }) {
                        Text("Annuler")
                    }
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
                    onClose = { isSearchExpanded = false }
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
        }
    ) { innerPadding ->
        // ✅ CORRECTION : Utiliser uniquement le padding du top
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
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
                        onLongClick = { viewModel.selectedConversations.add(conv.id) },
                        isSelected = viewModel.selectedConversations.contains(conv.id),
                    )
                }
            }
        }
    }
}