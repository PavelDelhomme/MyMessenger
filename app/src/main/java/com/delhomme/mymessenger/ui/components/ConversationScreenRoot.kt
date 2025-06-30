package com.delhomme.mymessenger.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.delhomme.mymessenger.ui.screen.ConversationListScreen

@Composable
fun ConversationRoot(navController: NavController) {
    val navController = rememberNavController()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("newConversation") }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        ConversationListScreen(
            onConversationClick = { id -> navController.navigate("messages/$id") },
            modifier = Modifier.padding(padding)
        )
    }
}
