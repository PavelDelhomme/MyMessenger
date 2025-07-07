package com.delhomme.mymessenger

import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
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

        Log.d("MAIN_ACTIVITY", "MainActivity created")

        setContent {
            MyMessengerTheme(darkTheme = isSystemInDarkTheme()) {
                MinimalPermissionsScreen(
                    onAllGranted = {
                        Log.d("MAIN_ACTIVITY", "onAllGranted called - initializing conversations")
                        viewModel.initializeConversations(this@MainActivity)
                    }
                ) {
                    Log.d("MAIN_ACTIVITY", "Showing main navigation")
                    AppNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MAIN_ACTIVITY", "MainActivity resumed")

        // Vérifier le statut SMS par défaut à chaque retour
        val defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this)
        val isDefault = defaultSmsApp == packageName
        Log.d("MAIN_ACTIVITY", "SMS default status on resume: $isDefault")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MAIN_ACTIVITY", "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == 1234) { // Code utilisé dans requestDefaultSmsApp
            val defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this)
            val isDefault = defaultSmsApp == packageName
            Log.d("MAIN_ACTIVITY", "SMS default request result: $isDefault")
        }
    }
}