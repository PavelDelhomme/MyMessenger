package com.delhomme.mymessenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.delhomme.mymessenger.ui.components.InitializationLoader
import com.delhomme.mymessenger.ui.navigation.AppNavigation
import com.delhomme.mymessenger.ui.screen.permissions.MinimalPermissionsScreen
import com.delhomme.mymessenger.ui.theme.MyMessengerTheme
import com.delhomme.mymessenger.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyMessengerTheme(darkTheme = isSystemInDarkTheme()) {
                val isInitializing by viewModel.isInitializing.collectAsStateWithLifecycle()
                val initializationProgress by viewModel.initializationProgress.collectAsStateWithLifecycle()
                val initializationMessage by viewModel.initializationMessage.collectAsStateWithLifecycle()

                var permissionsGranted by remember { mutableStateOf(false) }

                MinimalPermissionsScreen(
                    onAllGranted = {
                        if (!permissionsGranted) {
                            permissionsGranted = true
                            viewModel.initializeConversations(this@MainActivity)
                        }
                    }
                ) {
                    // Afficher le loader d'initialisation si nécessaire
                    InitializationLoader(
                        isVisible = isInitializing,
                        progress = initializationProgress,
                        message = initializationMessage
                    )

                    // Afficher l'app principale une fois l'initialisation terminée
                    if (!isInitializing) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}