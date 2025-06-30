package com.delhomme.mymessenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.data.repository.MessageRepository
import com.delhomme.mymessenger.data.repository.ConversationRepository
import com.delhomme.mymessenger.ui.components.RequestSmsPermissions
import com.delhomme.mymessenger.ui.navigation.AppNavigation
import com.delhomme.mymessenger.ui.screen.ConversationListScreen
import com.delhomme.mymessenger.ui.screen.MessageListScreen
import com.delhomme.mymessenger.ui.theme.MyMessengerTheme
import com.delhomme.mymessenger.viewmodel.ConversationViewModel
import com.delhomme.mymessenger.viewmodel.MessageViewModel

class MainActivity : ComponentActivity() {
    private lateinit var messageRepository: MessageRepository
    private lateinit var conversationRepository: ConversationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialisation des repositories
        messageRepository = MessageRepository(AppDatabase.getDatabase(this))
        conversationRepository = ConversationRepository(AppDatabase.getDatabase(this))

        setContent {
            MyMessengerTheme {
                RequestSmsPermissions {
                    AppNavigation()
                }
            }
        }
    }
}
