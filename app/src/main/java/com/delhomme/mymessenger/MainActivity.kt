package com.delhomme.mymessenger

import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.delhomme.mymessenger.ui.navigation.AppNavigation
import com.delhomme.mymessenger.ui.screen.permissions.MinimalPermissionsScreen
import com.delhomme.mymessenger.ui.theme.MyMessengerTheme
import com.delhomme.mymessenger.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var isDefaultSmsApp by mutableStateOf(false)

    private lateinit var defaultSmsLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enregistrer le launcher pour le résultat de la demande SMS par défaut
        defaultSmsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d("SMS_RESULT", "Result received from SMS default request")
            checkDefaultSmsStatus()
        }

        // Vérification initiale
        checkDefaultSmsStatus()

        setContent {
            MyMessengerTheme(darkTheme = isSystemInDarkTheme()) {
                MinimalPermissionsScreen(
                    onAllGranted = {
                        Log.d("MAIN_ACTIVITY", "onAllGranted called")
                        viewModel.initializeConversations(this@MainActivity)
                    }
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Vérifier le statut à chaque fois que l'activité reprend
        checkDefaultSmsStatus()
    }

    private fun checkDefaultSmsStatus() {
        val defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this)
        val newStatus = defaultSmsApp == packageName

        if (newStatus != isDefaultSmsApp) {
            isDefaultSmsApp = newStatus
            Log.d("SMS_STATUS", "Default SMS app status changed: $isDefaultSmsApp")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234) { // Code utilisé dans requestDefaultSmsApp
            Log.d("SMS_RESULT", "SMS default request completed")
            checkDefaultSmsStatus()
        }
    }
}