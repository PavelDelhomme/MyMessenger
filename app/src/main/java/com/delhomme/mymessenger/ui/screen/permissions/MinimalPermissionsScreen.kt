package com.delhomme.mymessenger.ui.screen.permissions

import android.Manifest
import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
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
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.POST_NOTIFICATIONS
    )

    val permissionState = rememberMultiplePermissionsState(permissions)
    val context = LocalContext.current
    val activity = context as? Activity
    var isDefaultSmsApp by remember { mutableStateOf(false) }
    var hasCheckedOnce by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val checkSmsStatus = {
        if (activity != null) {
            val newStatus = isDefaultSmsApp(activity)
            Log.d("SMS_STATUS", "SMS status check: $newStatus")
            isDefaultSmsApp = newStatus
            hasCheckedOnce = true
            newStatus
        } else {
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
        if (permissionState.allPermissionsGranted && !hasCheckedOnce) {
            Log.d("SMS_STATUS", "Initial SMS status check")
            val status = checkSmsStatus()
            if (status) {
                Log.d("SMS_STATUS", "Already default SMS app, proceeding")
            }
        }
    }

    // 3. Observer uniquement quand on revient de la sélection d'app SMS
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Ne vérifier que si on a déjà demandé à l'utilisateur de changer
                    if (permissionState.allPermissionsGranted && hasCheckedOnce && !isDefaultSmsApp) {
                        Log.d("SMS_STATUS", "Returned from SMS selection, checking status")
                        val newStatus = checkSmsStatus()
                        if (newStatus) {
                            Log.d("SMS_STATUS", "Now default SMS app!")
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

    // 4. Logique d'affichage simplifiée (sans duplication)
    when {
        !permissionState.allPermissionsGranted -> {
            Log.d("SMS_STATUS", "Waiting for permissions")
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("L'application a besoin d'autorisations pour fonctionner.\nVeuillez accepter toutes les permissions.")
            }
        }

        !isDefaultSmsApp && hasCheckedOnce -> {
            Log.d("SMS_STATUS", "Permissions OK, need SMS default status")
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = {
                    if (activity != null) {
                        Log.d("SMS_STATUS", "User requesting SMS default status")
                        Toast.makeText(context, "Sélectionnez votre application dans la liste", Toast.LENGTH_SHORT).show()
                        requestDefaultSmsApp(activity)
                    } else {
                        Log.e("SMS_STATUS", "Activity is null!")
                        Toast.makeText(context, "Erreur : Activity null", Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text("Définir comme application SMS par défaut")
                }
            }
        }

        else -> {
            // Soit on est déjà l'app par défaut, soit on n'a pas encore vérifié
            Log.d("SMS_STATUS", "Showing main content")
            LaunchedEffect(Unit) {
                onAllGranted()
            }
            content()
        }
    }
}