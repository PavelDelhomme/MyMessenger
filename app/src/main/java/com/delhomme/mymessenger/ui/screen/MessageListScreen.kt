package com.delhomme.mymessenger.ui.screen

import android.R.id.message
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.getValue
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.delhomme.mymessenger.R
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.ui.components.MessageBubble
import com.delhomme.mymessenger.utils.formatMessageDate
import com.delhomme.mymessenger.viewmodel.MessageViewModel
import kotlinx.coroutines.launch
import androidx.paging.compose.items


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageListScreen(
    conversationId: Long,
    navController: NavController,
    viewModel: MessageViewModel = hiltViewModel()
) {
    val messages = viewModel.getMessages(conversationId).collectAsLazyPagingItems()
    val context = LocalContext.current
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }
    var showOptions by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<MessageEntity?>(null) }

    // Faire défiler vers le bas lors de l'ajout de nouveaux messages
    LaunchedEffect(messages.itemCount) {
        if (messages.itemCount > 0) {
            scope.launch {
                scrollState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nom du contact") }, // Remplacer par le nom réel
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Recherche dans la conversation */ }) {
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
                            onClick = { /* Naviguer vers les détails de la conversation */ }
                        )
                        DropdownMenuItem(
                            text = { Text("Médias") },
                            onClick = { /* Naviguer vers les médias */ }
                        )
                        DropdownMenuItem(
                            text = { Text("Notifications") },
                            onClick = { /* Gérer les notifications */ }
                        )
                        DropdownMenuItem(
                            text = { Text("Effacer") },
                            onClick = { /* Effacer la conversation */ }
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                    modifier = Modifier.weight(1f),
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
                        viewModel.sendMessage(conversationId, messageText)
                        messageText = ""
                    },
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_send),
                        contentDescription = "Envoyer",
                        tint = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            reverseLayout = true,
            state = scrollState
        ) {
            items(
                items = messages,
                key = { message -> message.id } // Clé unique pour chaque message
            ) { message ->
                if (message != null) {
                    MessageBubble(
                        message = message,
                        onLongClick = {
                            selectedMessage = message
                            showDatePicker = true
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

fun shouldShowDate(index: Int, messages: LazyPagingItems<MessageEntity>): Boolean {
    if (index == messages.itemCount - 1) return true

    val current = messages[index]?.date ?: 0L
    val next = messages[index + 1]?.date ?: 0L

    // Afficher la date si différence > 5 minutes
    return (current - next) > 300000
}