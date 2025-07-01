package com.delhomme.mymessenger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.delhomme.mymessenger.ui.components.ConversationRoot
import com.delhomme.mymessenger.ui.screen.MessagesInConversationScreen
import com.delhomme.mymessenger.ui.screen.NewConversationScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "conversations"
    ) {
        composable("conversations") {
            ConversationRoot(navController = navController) // Utilisation de ConversationRoot
        }

        composable(
            route = "messages/{conversationId}?name={name}&addr={addr}",
            arguments = listOf(
                navArgument("conversationId") { type = NavType.LongType },
                navArgument("name") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("addr") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val convId = backStackEntry.arguments?.getLong("conversationId") ?: 0L
            MessagesInConversationScreen(
                conversationId = convId,
                navController = navController
            )
        }
        composable("newConversation") {
            NewConversationScreen(navController)
        }
    }
}
