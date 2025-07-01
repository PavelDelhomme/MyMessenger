package com.delhomme.mymessenger.ui.screen

import android.R.attr.key
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
import androidx.compose.runtime.key
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
import com.delhomme.mymessenger.ui.components.MessageBubble
import com.delhomme.mymessenger.utils.formatFrenchPhoneNumber
import com.delhomme.mymessenger.utils.formatMessageDate
import com.delhomme.mymessenger.viewmodel.MessageViewModel
import kotlinx.coroutines.launch
import okhttp3.Cache.Companion.key
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

    // Utilisation derivedStateOf pour optimisation des re-compositions
    val displayName by remember(nameParam, addrParam) {
        derivedStateOf {
            if (!nameParam.isNullOrBlank()) URLDecoder.decode(nameParam, "UTF-8")
            else formatFrenchPhoneNumber(addrParam)
        }
    }

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
            /*scope.launch {
                scrollState.animateScrollToItem(0)
            }*/
            scrollState.scrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(displayName, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    //.imePadding() // Ajout important pour le clavier
            ) {
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
                                phoneNumber = addrParam
                            )
                            messageText = ""
                            // Rafraîchir manuellement les messages
                            scope.launch {
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
            //reverseLayout = true,
            state = scrollState
        ) {
            items(                                                  // <-- overload standard de LazyListScope
                count = messages.itemCount,                         // nombre d’éléments
                key = { index -> messages[index]?.id ?: index }     // clé stable (facultatif mais recommandé)
            ) { index ->
                val message = messages[index]
                // Utiliser remember pour optimiser le rendu des bulles
                key(message!!.id) {
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