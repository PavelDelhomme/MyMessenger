package com.delhomme.mymessenger.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.*
/*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete*/
/*import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar*/
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search

/*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue*/
import androidx.compose.runtime.*
/*import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext*/
import androidx.compose.ui.platform.*
import androidx.compose.ui.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.ui.components.SearchBar
import com.delhomme.mymessenger.utils.formatFrenchPhoneNumber
import com.delhomme.mymessenger.viewmodel.MessageViewModel
import java.net.URLDecoder


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesInConversationScreen(
    conversationId: Long,
    navController: NavController,
    viewModel: MessageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val messages = viewModel.getMessages(conversationId).collectAsLazyPagingItems()

    // Récupérer les paramètres de navigation
    val nameParam = navController.currentBackStackEntry?.arguments?.getString("name")
    val addrParam = navController.currentBackStackEntry?.arguments?.getString("addr") ?: ""

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    var showOptions by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<MessageEntity?>(null) }

    val displayName = remember(nameParam, addrParam) {
        if (!nameParam.isNullOrBlank()) URLDecoder.decode(nameParam, "UTF-8")
        else formatFrenchPhoneNumber(addrParam)
    }
    var selectedMessageId by remember { mutableStateOf<Long?>(null) }
    var showMenuForMessageId by remember { mutableStateOf<Long?>(null) }
    var selectedMessages = remember { mutableStateListOf<Long>() }
    var replyToMessage by remember { mutableStateOf<MessageEntity?>(null) }
    // Utilisation derivedStateOf pour optimisation des re-compositions
    /*val displayName by remember(nameParam, addrParam) {
        derivedStateOf {
            if (!nameParam.isNullOrBlank()) URLDecoder.decode(nameParam, "UTF-8")
            else formatFrenchPhoneNumber(addrParam)
        }
    }*/

    val scrollState = rememberLazyListState()

    // Filtrage des messages
    val filteredMessages = remember(messages.itemSnapshotList.items, searchQuery) {
        if (searchQuery.isBlank()) messages.itemSnapshotList.items
        else messages.itemSnapshotList.items.filter { it?.body?.contains(searchQuery, true) == true }
    }

    // Scroll automatique vers le bas à chaque nouveau message
    LaunchedEffect(messages.itemCount) {
        if (messages.itemCount > 0) {
            scrollState.scrollToItem(0)
        }
    }

    LaunchedEffect(conversationId) {
        // Préchargement des données
    }

    LaunchedEffect(Unit) {
        // Vérification que la conversation existe
        val conversation = viewModel.getConversation(conversationId)
        if (conversation == null) {
            navController.popBackStack()
        }
    }
    BackHandler(enabled = selectedMessages.isNotEmpty()) {
        selectedMessages.clear()
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                // Barre de recherche active
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = { isSearchActive = false },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (selectedMessages.isNotEmpty()) {
                TopAppBar(
                    title = { Text("${selectedMessages.size} sélectionné(s)") },
                    actions = {
                        IconButton(onClick = {
                            viewModel.deleteMessages(selectedMessages)
                            selectedMessages.clear()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                        }
                        IconButton(onClick = {
                            viewModel.copyMessagesToClipboard(selectedMessages, context)
                        }) { Icon(Icons.Filled.ContentCopy, null) }

                        IconButton(onClick = { selectedMessages.clear() }) {
                            Icon(Icons.Default.Close, "Annuler")
                        }
                    }
                )
            } else {
                CenterAlignedTopAppBar(
                    title = { Text(displayName, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Filled.Search, "Rechercher")
                        }
                        IconButton(onClick = { showOptions = true }) {
                            Icon(Icons.Filled.MoreVert, "Options")
                        }
                        DropdownMenu(
                            expanded = showOptions,
                            onDismissRequest = { showOptions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Détails") },
                                onClick = { /* TODO */ }
                            )
                            DropdownMenuItem(
                                text = { Text("Médias") },
                                onClick = { /* TODO */ }
                            )
                            DropdownMenuItem(
                                text = { Text("Notifications") },
                                onClick = { /* TODO */ }
                            )
                            DropdownMenuItem(
                                text = { Text("Effacer") },
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    //.imePadding() // Ajout important pour le clavier
            ) {
                if (replyToMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = replyToMessage!!.body.take(50),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { replyToMessage = null }) {
                            Icon(Icons.Default.Close, "Annuler la réponse")
                        }
                    }
                }

                // Barre de saisie du message
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Ajouter pièce jointe */ }) {
                        Icon(Icons.Filled.AddCircle, "Pièce jointe")
                    }
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp, bottom = 0.dp),
                        placeholder = { Text("Message...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    IconButton(
                        onClick = {
                            // Envoyer le message
                            viewModel.sendMessage(
                                conversationId = conversationId,
                                text = messageText,
                                phoneNumber = addrParam,
                                replyToId = replyToMessage?.id
                            )
                            messageText = ""
                            // Rafraîchir manuellement les messages
                            replyToMessage = null
                            coroutineScope.launch {
                                messages.refresh()
                                scrollState.animateScrollToItem(0)
                            }
                        },
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send),
                            contentDescription = "Envoyer",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            reverseLayout = true, // Les messages récents sont en bas ?
            state = scrollState
        ) {
            items(filteredMessages.size) { index ->
                val message = filteredMessages[index]
                if (message != null) {
                    MessageItem(
                        message = message,
                        isSelected = selectedMessages.contains(message.id),
                        onLongClick = {
                            // Sélection multiple par long click
                            if (selectedMessages.contains(message.id)) {
                                selectedMessages.remove(message.id)
                            } else {
                                selectedMessages.add(message.id)
                            }
                        },
                        onClick = {
                            if (selectedMessages.isNotEmpty()) {
                                if (selectedMessages.contains(message.id)) {
                                    selectedMessages.remove(message.id)
                                } else {
                                    selectedMessages.add(message.id)
                                }
                            } else {
                                // Sinon afficher menu contextuel
                                showMenuForMessageId = message.id
                            }
                        },
                        showMenu = showMenuForMessageId == message.id,
                        onMenuDismiss = { showMenuForMessageId = null },
                        onCopy = { /* copier message.body dans clipboard */ },
                        onDelete = { /* supprimer le message */ },
                        onForward = { /* transférer le message */ },
                        onReply = {
                            replyToMessage = message
                            showMenuForMessageId = null
                        }
                    )
                }
            }
        }
    }

    // Afficher la date complète au clic
    if (showDatePicker && selectedMessage != null) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Détails du message") },
            text = { Text("Date complète: ${formatMessageDate(selectedMessage!!.date, full = true)}") },
            confirmButton = {
                Button(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            }
        )
    }
}