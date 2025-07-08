package com.delhomme.mymessenger.ui.screen

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.delhomme.mymessenger.R
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.ui.components.MessageItem
import com.delhomme.mymessenger.ui.screen.permissions.WithPermission
import com.delhomme.mymessenger.utils.formatFrenchPhoneNumber
import com.delhomme.mymessenger.utils.formatMessageDate
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
    // Récupérer les paramètres de navigation
    val nameParam = navController.currentBackStackEntry?.arguments?.getString("name")
    val addrParam = navController.currentBackStackEntry?.arguments?.getString("addr") ?: ""

    val messages = viewModel.getMessages(conversationId).collectAsLazyPagingItems()
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }

    // Media picker pour sélectionner des fichiers
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        attachmentUri = uri
    }

    val context = LocalContext.current
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }
    var showOptions by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var selectedMessages = remember { mutableStateListOf<Long>() }
    var showMenuForMessageId by remember { mutableStateOf<Long?>(null) }
    var replyToMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Filtrage
    val filteredMessages = remember(messages.itemSnapshotList.items, searchQuery) {
        if (searchQuery.isBlank()) messages.itemSnapshotList.items
        else messages.itemSnapshotList.items.filter { it?.body?.contains(searchQuery, true) == true }
    }

    LaunchedEffect(conversationId) {
        println("DEBUG: conversationId utilisé dans l'écran = $conversationId")
        viewModel.logMessagesForConversation(conversationId)
    }

    LaunchedEffect(Unit) {
        // Vérification que la conversation existe
        val conversation = viewModel.getConversation(conversationId)
        if (conversation == null) {
            navController.popBackStack()
        }
    }

    // Utilisation derivedStateOf pour optimisation des re-compositions
    val displayName by remember(nameParam, addrParam) {
        derivedStateOf {
            if (!nameParam.isNullOrBlank()) URLDecoder.decode(nameParam, "UTF-8")
            else formatFrenchPhoneNumber(addrParam)
        }
    }

    // Faire défiler vers le bas lors de l'ajout de nouveaux messages
    LaunchedEffect(messages.itemCount) {
        if (messages.itemCount > 0) {
            scrollState.scrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(displayName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
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
                            onClick = {
                                showOptions = false
                                /* Naviguer vers les détails de la conversation */
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Médias") },
                            onClick = {
                                showOptions = false
                                /* Naviguer vers les médias */
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Notifications") },
                            onClick = {
                                showOptions = false
                                /* Gérer les notifications */
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Effacer") },
                            onClick = {
                                showOptions = false
                                /* Effacer la conversation */
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Barre de réponse
                if (replyToMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Réponse à: ${replyToMessage!!.body.take(50)}${if (replyToMessage!!.body.length > 50) "..." else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { replyToMessage = null }) {
                            Icon(Icons.Filled.Close, "Annuler la réponse")
                        }
                    }
                }

                // Prévisualisation de l'attachement
                if (attachmentUri != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fichier sélectionné",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { attachmentUri = null }) {
                            Icon(Icons.Filled.Close, "Supprimer l'attachement")
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
                    IconButton(onClick = {
                        mediaPickerLauncher.launch("*/*")
                    }) {
                        Icon(Icons.Filled.AddCircle, "Pièce jointe")
                    }

                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Message...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    WithPermission(
                        permission = Manifest.permission.SEND_SMS,
                        onAction = {
                            if (messageText.isNotBlank()) {
                                scope.launch {
                                    try {
                                        // Si on a un attachement, envoyer un MMS
                                        if (attachmentUri != null) {
                                            viewModel.sendMmsMessage(
                                                conversationId = conversationId,
                                                text = messageText,
                                                phoneNumber = addrParam,
                                                mediaUri = attachmentUri!!,
                                                replyToId = replyToMessage?.id
                                            )
                                        } else {
                                            // Sinon envoyer un SMS
                                            viewModel.sendMessage(
                                                conversationId = conversationId,
                                                text = messageText,
                                                phoneNumber = addrParam,
                                                replyToId = replyToMessage?.id
                                            )
                                        }

                                        // Reset après envoi
                                        messageText = ""
                                        attachmentUri = null
                                        replyToMessage = null

                                        // Faire défiler vers le bas
                                        messages.refresh()
                                        scrollState.animateScrollToItem(0)

                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Erreur lors de l'envoi: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        },
                        onPermissionDenied = {
                            Toast.makeText(
                                context,
                                "Permission SMS requise pour envoyer",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) { requestPermission ->
                        IconButton(
                            onClick = { requestPermission() },
                            enabled = messageText.isNotBlank(),
                            modifier = Modifier
                                .background(
                                    color = if (messageText.isNotBlank())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                                .size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_send),
                                contentDescription = "Envoyer",
                                tint = if (messageText.isNotBlank())
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Barre de recherche
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Rechercher dans la conversation...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, "Rechercher")
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Filled.Close, "Fermer")
                        }
                    }
                )
            }

            // Liste des messages
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = scrollState,
                reverseLayout = true
            ) {
                if (filteredMessages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Aucun message à afficher")
                            Text("ID conversation: $conversationId", style = MaterialTheme.typography.bodySmall)
                            Text("Nombre de messages: ${messages.itemCount}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    items(filteredMessages.size) { index ->
                        val message = filteredMessages[index]
                        if (message != null) {
                            // Charger le message cité si besoin
                            var repliedMessage by remember { mutableStateOf<MessageEntity?>(null) }
                            LaunchedEffect(message.replyToId) {
                                repliedMessage = message.replyToId?.let { viewModel.getMessageById(it) }
                            }

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
                                        // En mode sélection multiple
                                        if (selectedMessages.contains(message.id)) {
                                            selectedMessages.remove(message.id)
                                        } else {
                                            selectedMessages.add(message.id)
                                        }
                                    } else {
                                        // Afficher menu contextuel
                                        showMenuForMessageId = message.id
                                    }
                                },
                                showMenu = showMenuForMessageId == message.id,
                                onMenuDismiss = { showMenuForMessageId = null },
                                onCopy = {
                                    viewModel.copyMessagesToClipboard(listOf(message.id), context)
                                    Toast.makeText(context, "Message copié", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    // TODO: Implémenter la suppression
                                    Toast.makeText(context, "Suppression à implémenter", Toast.LENGTH_SHORT).show()
                                },
                                onForward = {
                                    // TODO: Implémenter le transfert
                                    Toast.makeText(context, "Transfert à implémenter", Toast.LENGTH_SHORT).show()
                                },
                                onReply = {
                                    replyToMessage = message
                                    showMenuForMessageId = null
                                },
                                repliedMessage = repliedMessage
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog de détails du message
    if (showDatePicker && selectedMessage != null) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Détails du message") },
            text = {
                Text("Date complète: ${formatMessageDate(selectedMessage!!.date, full = true)}")
            },
            confirmButton = {
                Button(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            }
        )
    }
}