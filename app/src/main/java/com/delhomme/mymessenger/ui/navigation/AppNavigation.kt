package com.delhomme.mymessenger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.delhomme.mymessenger.ui.screen.ConversationListScreen
import com.delhomme.mymessenger.ui.screen.MessageListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "conversations"
    ) {
        composable("conversations") {
            ConversationListScreen(
                onConversationClick = { convId ->
                    navController.navigate("messages/$convId")
                }
            )
        }
        composable("messages/{conversationId}") { backStackEntry ->
            val convId = backStackEntry.arguments?.getString("conversationId")?.toLongOrNull() ?: 0L
            MessageListScreen(conversationId = convId)
        }
    }
}
