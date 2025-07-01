package com.delhomme.mymessenger.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.delhomme.mymessenger.ui.components.ConversationRoot
import com.delhomme.mymessenger.ui.components.DrawerComponent
import com.delhomme.mymessenger.ui.screen.ArchiveScreen
import com.delhomme.mymessenger.ui.screen.ConversationListScreen
import com.delhomme.mymessenger.ui.screen.MessagesInConversationScreen
import com.delhomme.mymessenger.ui.screen.NewConversationScreen
import com.delhomme.mymessenger.ui.screen.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerComponent(
                navController = navController,
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {

        NavHost(
            navController = navController,
            startDestination = "conversations"
        ) {

            composable("conversations") {
                ConversationListScreen(
                    onConversationClick = { convId ->
                        navController.navigate("messages/$convId")
                    },
                    onAddClick = { navController.navigate("newConversation") },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    navController = navController
                )
            }
            /*
            composable("conversations") {
                ConversationRoot(navController = navController) // Utilisation de ConversationRoot
            }
             */

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
            composable("archive") { ArchiveScreen(navController) }
            composable("trash") { /* Écran Corbeille : TrashScreen(navController) */ }
            composable("spam") { /* Écran Spam : SpamScreen(navController) */ }
            composable("settings") {
                SettingsScreen(navController)
            }

        }
    }
}
