package com.delhomme.mymessenger.ui.screen

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.delhomme.mymessenger.R
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.ui.components.*
import com.delhomme.mymessenger.ui.screen.permissions.WithPermission
import com.delhomme.mymessenger.utils.formatFrenchPhoneNumber
import com.delhomme.mymessenger.viewmodel.MessageViewModel
import kotlinx.coroutines.launch
import java.net.URLDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesInConversationScreen(
    conversationId: Long,
    navController: NavController,
    viewModel: MessageViewModel = hiltViewModel()
) {
    // Paramètres de navigation
    val nameParam = navController.currentBackStackEntry?.arguments?.getString("name")
    val addrParam = navController.currentBackStackEntry?.arguments?.getString("addr") ?: ""

    val messages = viewModel.getMessages(conversationId).collectAsLazyPagingItems()
    val context = LocalContext.current
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // États de l'interface
    var messageText by remember { mutableStateOf("") }
    var showOptions by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var selectedMessages = remember { mutableStateListOf<Long>() }
    var showMenuForMessageId by remember { mutableStateOf<Long?>(null) }
    var replyToMessage by remember { mutableStateOf<MessageEntity?>(null) }

    // États pour les nouvelles fonctionnalités
    var showAttachmentPanel by remember { mutableStateOf(false) }
    var attachments by remember { mutableStateOf<List<AttachmentItem>>(emptyList()) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var scheduledMessage by remember { mutableStateOf<ScheduledMessage?>(null) }

    // État de recherche
    val searchState = remember { ConversationSearchState() }
    val isSearchActive by searchState.isSearchActive
    val searchQuery by searchState.searchQuery
    val searchResults by searchState.searchResults
    val currentResultIndex by searchState.currentResultIndex

    // Gestion de la recherche
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            // Simuler la recherche dans les messages
            val results = messages.itemSnapshotList.items.mapNotNull { message ->
                message?.let {
                    val matches = it.body.findSearchMatches(searchQuery)
                    if (matches.isNotEmpty()) {
                        SearchResult(
                            messageId = it.id,
                            snippet = it.body,
                            matchStart = matches.first().first,
                            matchEnd = matches.first().second
                        )
                    } else null
                }
            }
            searchState.updateSearchResults(results)
        }
    }

    // Scroll vers le résultat de recherche
    LaunchedEffect(currentResultIndex, searchResults) {
        searchState.getCurrentResult()?.let { result ->
            val messageIndex = messages.itemSnapshotList.items.indexOfFirst { it?.id == result.messageId }
            if (messageIndex >= 0) {
                scrollState.animateScrollToItem(messageIndex)
            }
        }
    }

    val displayName by remember(nameParam, addrParam) {
        derivedStateOf {
            if (!nameParam.isNullOrBlank()) URLDecoder.decode(nameParam, "UTF-8")
            else formatFrenchPhoneNumber(addrParam)
        }
    }

    // Auto-scroll pour nouveaux messages
    LaunchedEffect(messages.itemCount) {
        if (messages.itemCount > 0 && scrollState.firstVisibleItemIndex < 3) {
            scrollState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            if (!isSearchActive) {
                CenterAlignedTopAppBar(
                    title = { Text(displayName, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                        }
                    },
                    actions = {
                        IconButton(onClick = { searchState.activateSearch() }) {
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
                                onClick = { showOptions = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Médias") },
                                onClick = { showOptions = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Messages programmés") },
                                onClick = {
                                    // TODO: Afficher la liste des messages programmés
                                    showOptions = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Effacer") },
                                onClick = { showOptions = false }
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
            ) {
                // Barre de recherche
                ConversationSearchBar(
                    isVisible = isSearchActive,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchState.updateSearchQuery(it) },
                    searchResults = searchResults,
                    currentResultIndex = currentResultIndex,
                    onPrevious = { searchState.goToPrevious() },
                    onNext = { searchState.goToNext() },
                    onClose = { searchState.deactivateSearch() },
                    onGoToResult = { result ->
                        // Scroll vers le résultat
                        scope.launch {
                            val messageIndex = messages.itemSnapshotList.items.indexOfFirst { it?.id == result.messageId }
                            if (messageIndex >= 0) {
                                scrollState.animateScrollToItem(messageIndex)
                            }
                        }
                    }
                )

                // Bannière message programmé
                scheduledMessage?.let { scheduled ->
                    ScheduledMessageBanner(
                        scheduledMessage = scheduled,
                        onCancel = { scheduledMessage = null },
                        onEdit = { showScheduleDialog = true }
                    )
                }

                // Panneau de pièces jointes
                AttachmentPanel(
                    isVisible = showAttachmentPanel,
                    onAttachmentSelected = { attachment ->
                        attachments = attachments + attachment
                    },
                    onDismiss = { showAttachmentPanel = false }
                )

                // Aperçu des pièces jointes
                AttachmentPreview(
                    attachments = attachments,
                    onRemoveAttachment = { attachment ->
                        attachments = attachments - attachment
                    }
                )

                // Barre de réponse
                if (replyToMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.reply),
                            contentDescription = "Réponse",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Répondre à : ${replyToMessage!!.body.take(50)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { replyToMessage = null }) {
                            Icon(Icons.Filled.Close, "Annuler la réponse")
                        }
                    }
                }

                // Barre de saisie du message
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(24.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bouton plus (pièces jointes)
                    IconButton(
                        onClick = { showAttachmentPanel = !showAttachmentPanel }
                    ) {
                        Icon(
                            imageVector = if (showAttachmentPanel) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = if (showAttachmentPanel) "Fermer" else "Pièces jointes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 4
                    )

                    // Bouton programmation
                    IconButton(
                        onClick = { showScheduleDialog = true },
                        enabled = messageText.isNotBlank() || attachments.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Programmer",
                            tint = if (messageText.isNotBlank() || attachments.isNotEmpty())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline
                        )
                    }

                    // Bouton d'envoi
                    WithPermission(
                        permission = Manifest.permission.SEND_SMS,
                        onAction = {
                            if (messageText.isNotBlank() || attachments.isNotEmpty()) {
                                viewModel.sendMessage(
                                    conversationId = conversationId,
                                    phoneNumber = addrParam,
                                    replyToId = replyToMessage?.id,
                                    text = messageText
                                    // TODO: Ajouter support des attachments
                                )
                                messageText = ""
                                attachments = emptyList()
                                replyToMessage = null
                                showAttachmentPanel = false

                                scope.launch {
                                    scrollState.animateScrollToItem(0)
                                }
                            }
                        },
                        onPermissionDenied = {
                            Toast.makeText(context, "Permission SMS requise", Toast.LENGTH_SHORT).show()
                        }
                    ) { requestPermission ->
                        IconButton(
                            onClick = { requestPermission() },
                            enabled = messageText.isNotBlank() || attachments.isNotEmpty(),
                            modifier = Modifier
                                .background(
                                    color = if (messageText.isNotBlank() || attachments.isNotEmpty())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_send),
                                contentDescription = "Envoyer",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                messages.loadState.refresh is LoadState.Loading -> {
                    SimpleMessageLoader(
                        isVisible = true,
                        message = "Chargement des messages..."
                    )
                }

                messages.loadState.refresh is LoadState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_error),
                            contentDescription = "Erreur",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Erreur lors du chargement",
                            modifier = Modifier.padding(top = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { messages.retry() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Réessayer")
                        }
                    }
                }

                messages.itemCount == 0 -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_message),
                            contentDescription = "Aucun message",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Aucun message dans cette conversation",
                            modifier = Modifier.padding(top = 16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Commencez à échanger !",
                            modifier = Modifier.padding(top = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = scrollState,
                        reverseLayout = true,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        items(
                            count = messages.itemCount,
                            key = { index -> messages[index]?.id ?: index }
                        ) { index ->
                            val message = messages[index]
                            message?.let {
                                var repliedMessage by remember { mutableStateOf<MessageEntity?>(null) }
                                LaunchedEffect(message.replyToId) {
                                    repliedMessage = message.replyToId?.let { viewModel.getMessageById(it) }
                                }

                                // Message avec surbrillance de recherche si applicable
                                val isHighlighted = searchResults.any { it.messageId == message.id }
                                val backgroundColor = if (isHighlighted && searchState.getCurrentResult()?.messageId == message.id) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                } else Color.Transparent

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(backgroundColor, RoundedCornerShape(8.dp))
                                        .padding(if (isHighlighted) 4.dp else 0.dp)
                                ) {
                                    MessageItem(
                                        message = message,
                                        isSelected = selectedMessages.contains(message.id),
                                        onLongClick = {
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
                                                showMenuForMessageId = message.id
                                            }
                                        },
                                        showMenu = showMenuForMessageId == message.id,
                                        onMenuDismiss = { showMenuForMessageId = null },
                                        onCopy = {
                                            viewModel.copyMessagesToClipboard(listOf(message.id), context)
                                            showMenuForMessageId = null
                                        },
                                        onDelete = {
                                            // TODO: Implémenter suppression
                                            showMenuForMessageId = null
                                        },
                                        onForward = {
                                            // TODO: Implémenter transfert
                                            showMenuForMessageId = null
                                        },
                                        onReply = {
                                            replyToMessage = message
                                            showMenuForMessageId = null
                                        },
                                        repliedMessage = repliedMessage,
                                        searchQuery = if (isHighlighted) searchQuery else ""
                                    )
                                }
                            }
                        }

                        // Indicateur de chargement
                        item {
                            if (messages.loadState.append is LoadState.Loading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog de programmation
    ScheduleMessageDialog(
        isVisible = showScheduleDialog,
        onDismiss = { showScheduleDialog = false },
        onSchedule = { scheduledTime ->
            scheduledMessage = ScheduledMessage(
                message = messageText,
                attachments = attachments,
                scheduledTime = scheduledTime,
                phoneNumber = addrParam,
                conversationId = conversationId
            )
            messageText = ""
            attachments = emptyList()
            showScheduleDialog = false

            Toast.makeText(context, "Message programmé", Toast.LENGTH_SHORT).show()
        }
    )

    // Dialog détails du message
    if (showDatePicker && selectedMessage != null) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Détails du message") },
            text = {
                Text("Date complète: ${com.delhomme.mymessenger.utils.formatMessageDate(selectedMessage!!.date, full = true)}")
            },
            confirmButton = {
                Button(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            }
        )
    }
}