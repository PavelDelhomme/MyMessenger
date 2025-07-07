package com.delhomme.mymessenger.ui.screen.permissions

import android.Manifest
import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import android.provider.Telephony
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.delhomme.mymessenger.utils.isDefaultSmsApp
import com.delhomme.mymessenger.utils.requestDefaultSmsApp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MinimalPermissionsScreen(
    onAllGranted: () -> Unit,
    content: @Composable () -> Unit
) {
    val permissions = listOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.RECEIVE_MMS,
        Manifest.permission.RECEIVE_WAP_PUSH,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.READ_PHONE_STATE
    )

    val permissionState = rememberMultiplePermissionsState(permissions)
    val context = LocalContext.current
    val activity = context as? Activity
    var isDefaultSmsApp by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Fonction pour vérifier le statut SMS avec la nouvelle méthode renforcée
    val checkSmsStatus = {
        if (activity != null) {
            val newStatus = isDefaultSmsApp(activity)
            Log.d("SMS_STATUS", "Enhanced SMS status check: $newStatus")
            isDefaultSmsApp = newStatus
            newStatus
        } else {
            Log.w("SMS_STATUS", "Activity is null, cannot check SMS status")
            false
        }
    }

    // 1. Demande toutes les permissions d'abord
    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted) {
            Log.d("SMS_STATUS", "Requesting permissions...")
            permissionState.launchMultiplePermissionRequest()
        }
    }

    // 2. Vérification initiale du statut SMS quand les permissions sont accordées
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            Log.d("SMS_STATUS", "All permissions granted, checking SMS status")
            checkSmsStatus()
        }
    }

    // 3. Observer le cycle de vie pour détecter quand l'utilisateur revient
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d("SMS_STATUS", "Activity resumed, checking SMS status")
                    if (permissionState.allPermissionsGranted) {
                        val newStatus = checkSmsStatus()
                        if (newStatus) {
                            Log.d("SMS_STATUS", "App is now default SMS app!")
                        }
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 4. Vérification continue quand les permissions sont accordées mais pas encore SMS par défaut
    LaunchedEffect(permissionState.allPermissionsGranted, isDefaultSmsApp) {
        if (permissionState.allPermissionsGranted && !isDefaultSmsApp) {
            Log.d("SMS_STATUS", "Starting continuous SMS status monitoring")
            // Vérification toutes les 2 secondes pendant maximum 60 secondes
            var attempts = 0
            while (attempts < 30 && !isDefaultSmsApp) {
                delay(2000) // Délai plus long pour éviter le spam
                val newStatus = checkSmsStatus()
                if (newStatus) {
                    Log.d("SMS_STATUS", "SMS status changed to default! Stopping monitoring.")
                    break
                }
                attempts++
                Log.d("SMS_STATUS", "SMS check attempt $attempts/30")
            }

            if (attempts >= 30) {
                Log.w("SMS_STATUS", "SMS status monitoring timed out after 30 attempts")
            }
        }
    }

    // 5. Logique d'affichage
    when {
        !permissionState.allPermissionsGranted -> {
            Log.d("SMS_STATUS", "Waiting for permissions")
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("L'application a besoin d'autorisations pour fonctionner.\nVeuillez accepter toutes les permissions.")
            }
        }

        !isDefaultSmsApp -> {
            Log.d("SMS_STATUS", "Permissions OK, waiting for SMS default status")
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = {
                    activity?.let {
                        Log.d("SMS_STATUS", "User clicked button to set as default SMS app")
                        requestDefaultSmsApp(it)
                    }
                }) {
                    Text("Définir comme application SMS par défaut")
                }
            }
        }

        else -> {
            Log.d("SMS_STATUS", "All conditions met, showing main content")
            // Appeler onAllGranted une seule fois
            LaunchedEffect(Unit) {
                Log.d("SMS_STATUS", "Calling onAllGranted callback")
                onAllGranted()
            }
            content()
        }
    }
}