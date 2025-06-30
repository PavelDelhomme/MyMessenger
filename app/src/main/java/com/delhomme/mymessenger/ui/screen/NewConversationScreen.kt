package com.delhomme.mymessenger.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.delhomme.mymessenger.utils.getAllContacts



@Composable
fun NewConversationScreen(nav: NavController) {
    val context = LocalContext.current
    val contacts = remember { mutableStateOf(emptyList<Pair<String, String>>()) }

    LaunchedEffect(Unit) {
        contacts.value = getAllContacts(context)
    }

    LazyColumn {
        items(contacts.value, key = { it.first }) { (number, name) ->
            ListItem(
                headlineContent = { Text(name) },
                supportingContent = { Text(number) },
                modifier = Modifier.clickable {
                    nav.navigate("messages/${number.hashCode()}?addr=$number")
                }
            )
        }
    }
}