package com.delhomme.mymessenger.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.delhomme.mymessenger.ui.components.SearchBar
import com.delhomme.mymessenger.utils.getAllContacts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewConversationScreen(nav: NavController) {
    val context = LocalContext.current
    val contactsState = remember { mutableStateOf(emptyList<Pair<String, String>>()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // Récupération des contacts
            val contactList = getAllContacts(context)
            // Mise à jour de l'état dans le dispatcher principal
            withContext(Dispatchers.Main) {
                contactsState.value = contactList
                isLoading = false
            }
        }
    }


    // Filtrage des contacts basé sur la recherche
    val filteredContacts = remember(contactsState.value, searchQuery) {
        if (searchQuery.isBlank()) {
            contactsState.value
        } else {
            val queryLower = searchQuery.lowercase()
            contactsState.value.filter { (number, name) ->
                name.lowercase().contains(queryLower) || number.contains(searchQuery)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sélectionner un contact") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Barre de recherche pour les contacts
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Rechercher des contacts...",
                modifier = Modifier.padding(16.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredContacts, key = { it.first }) { contact ->
                        val (number, name) = contact
                        ListItem(
                            headlineContent = { Text(name) },
                            supportingContent = { Text(number) },
                            modifier = Modifier
                                .clickable {
                                    nav.navigate("messages/${number.hashCode()}?addr=$number")
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}