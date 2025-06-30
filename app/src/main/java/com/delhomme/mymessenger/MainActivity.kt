package com.delhomme.mymessenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.delhomme.mymessenger.ui.components.RequestSmsPermissions
import com.delhomme.mymessenger.ui.screen.ConversationListScreen
import com.delhomme.mymessenger.ui.theme.MyMessengerTheme
import com.delhomme.mymessenger.viewmodel.ConversationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyMessengerTheme {
                RequestSmsPermissions {
                    ConversationListScreen(

                        viewModel = ConversationViewModel(),
                        onConversationClick = { convId ->
                            // Navigation vers l'écran de messages
                        }
                    )
                }
            }
        }
    }
}