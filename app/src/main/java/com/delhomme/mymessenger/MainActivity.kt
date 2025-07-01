package com.delhomme.mymessenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import com.delhomme.mymessenger.ui.components.RequestSmsPermissions
import com.delhomme.mymessenger.ui.navigation.AppNavigation
import com.delhomme.mymessenger.ui.theme.MyMessengerTheme
import com.delhomme.mymessenger.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyMessengerTheme(darkTheme = isSystemInDarkTheme()) {
                RequestSmsPermissions(
                    content = { AppNavigation() },
                    onPermissionsGranted = {
                        viewModel.initializeConversations(this@MainActivity)
                    }
                )
            }
        }
    }
}
