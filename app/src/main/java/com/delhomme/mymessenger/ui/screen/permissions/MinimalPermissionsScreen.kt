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
import com.delhomme.mymessenger.utils.requestDefaultSmsApp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

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
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.POST_NOTIFICATIONS
    )
    val permissionState = rememberMultiplePermissionsState(permissions)
    val context = LocalContext.current
    val activity = context as? Activity
    var isDefaultSmsApp by remember { mutableStateOf(false) }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            val defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context)
            isDefaultSmsApp = defaultSmsApp == context.packageName
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
            onAllGranted()
            content()
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("L'application a besoin d'autorisations pour fonctionner.")
        }
    }
}