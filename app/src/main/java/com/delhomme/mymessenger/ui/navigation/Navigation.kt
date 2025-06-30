package com.delhomme.mymessenger.ui.navigation

import androidx.compose.runtime.Composable
import com.delhomme.mymessenger.ui.screen.ConversationListScreen
import com.delhomme.mymessenger.ui.screen.MessageListScreen

@Composable

fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "conversations") {
        composable("conversations") {
            ConversationListScreen(
                viewModel = hiltViewModel(),
                onConversationClick = { convId ->
                    navController.navigate("messages/$convId")
                }
            )
        }
        composable("messages/{conversationId}") { backStackEntry ->
            val convId = backStackEntry.arguments?.getString("conversationId")?.toLongOrNull() ?: 0L
            MessageListScreen(viewModel = hiltViewModel(), conversationId = convId)
        }
    }
}
