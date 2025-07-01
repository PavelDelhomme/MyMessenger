package com.delhomme.mymessenger.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.delhomme.mymessenger.ui.screen.ConversationListScreen
import kotlinx.coroutines.launch

@Composable
fun ConversationRoot(navController: NavController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("newConversation") }) {
                Icon(Icons.Default.Add, contentDescription = "Add conversation")
            }
        }
    ) { padding ->
        ConversationListScreen(
            onConversationClick = { convId -> navController.navigate("messages/$convId") },
            onAddClick = { navController.navigate("newConversation") }, // Ajouté
            onMenuClick = { scope.launch { drawerState.open() } },
            navController = navController
        )
    }
}
