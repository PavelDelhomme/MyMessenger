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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
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

    // 1. Demande toutes les permissions d'abord
    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted) {
            permissionState.launchMultiplePermissionRequest()
        }
    }
    // 2. Vérification continue du statut SMS par défaut quand l'activité est active
    LaunchedEffect(permissionState.allPermissionsGranted, lifecycleOwner) {
        if (permissionState.allPermissionsGranted) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    val defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context)
                    val currentStatus = defaultSmsApp == context.packageName

                    if (currentStatus != isDefaultSmsApp) {
                        isDefaultSmsApp = currentStatus
                        Log.d("SMS_STATUS", "SMS default status updated: $isDefaultSmsApp")

                        if (isDefaultSmsApp) {
                            Log.d("SMS_STATUS", "App is now default SMS app, calling onAllGranted")
                            break // Sortir de la boucle quand on devient l'app par défaut
                        }
                    }

                    delay(500) // Vérifier toutes les 500ms
                }
            }
        }
    }


    // 3. Vérification initiale du statut
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            val defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context)
            isDefaultSmsApp = defaultSmsApp == context.packageName
            Log.d("SMS_STATUS", "Initial SMS status check: $isDefaultSmsApp")
        }
    }


    // 4. Logique d'affichage
    when {
        !permissionState.allPermissionsGranted -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("L'application a besoin d'autorisations pour fonctionner.")
            }
        }

        !isDefaultSmsApp -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = {
                    activity?.let {
                        Log.d("SMS_STATUS", "Requesting default SMS app status")
                        requestDefaultSmsApp(it)
                    }
                }) {
                    Text("Définir comme application SMS par défaut")
                }
            }
        }

        else -> {
            Log.d("SMS_STATUS", "All permissions granted and is default SMS app")
            onAllGranted()
            content()
        }
    }
    // 4. Logique d'affichage
    when {
        !permissionState.allPermissionsGranted -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("L'application a besoin d'autorisations pour fonctionner.")
            }
        }

        !isDefaultSmsApp -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = {
                    activity?.let {
                        Log.d("SMS_STATUS", "Requesting default SMS app status")
                        requestDefaultSmsApp(it)
                    }
                }) {
                    Text("Définir comme application SMS par défaut")
                }
            }
        }

        else -> {
            Log.d("SMS_STATUS", "All permissions granted and is default SMS app")
            onAllGranted()
            content()
        }
    }

    // ✅ CORRECTION : Vérification immédiate au retour de l'activité
    /*LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            // Vérification immédiate quand l'activité reprend
            val defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context)
            val newStatus = defaultSmsApp == context.packageName
            if (newStatus != isDefaultSmsApp) {
                isDefaultSmsApp = newStatus
                Log.d("SMS", "SMS default status updated: $isDefaultSmsApp")
            }

            // Puis vérification continue
            while (true) {
                delay(1000)
                val currentDefaultApp = Telephony.Sms.getDefaultSmsPackage(context)
                val currentStatus = currentDefaultApp == context.packageName
                if (currentStatus != isDefaultSmsApp) {
                    isDefaultSmsApp = currentStatus
                    Log.d("SMS", "SMS default status updated: $isDefaultSmsApp")
                    if (isDefaultSmsApp) {
                        break // Arrêter la boucle si on devient l'app par défaut
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted) {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    if (permissionState.allPermissionsGranted) {
        if (!isDefaultSmsApp) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = {
                    if (activity != null) {
                        Log.d("SMS", "Button clicked, Activity OK")
                        Toast.makeText(context, "Sélectionnez votre application dans la liste", Toast.LENGTH_SHORT).show()
                        requestDefaultSmsApp(activity)
                    } else {
                        Log.e("SMS", "Activity is null!")
                        Toast.makeText(context, "Erreur : Activity null", Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text("Définir comme application SMS par défaut")
                }
            }
        } else {
            Log.d("SMS", "L'application est déjà définie comme SMS par défaut, onAllGranted")
            onAllGranted()
            content()
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("L'application a besoin d'autorisations pour fonctionner.")
        }
    }*/
}