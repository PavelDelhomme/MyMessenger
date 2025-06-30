package com.delhomme.mymessenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.delhomme.mymessenger.ui.components.RequestSmsPermissions
import com.delhomme.mymessenger.ui.navigation.AppNavigation
import com.delhomme.mymessenger.ui.theme.MyMessengerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyMessengerTheme {
                RequestSmsPermissions {
                    AppNavigation()
                }
            }
        }
    }
}
