package com.delhomme.mymessenger.ui.components

import android.Manifest
import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestSmsPermissions(
    content: @Composable () -> Unit,
    onPermissionsGranted: () -> Unit
) {
    val permissions = listOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_PHONE_STATE
    )

    val permissionState = rememberMultiplePermissionsState(permissions)
    var showRationale by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(permissionState) {
        if (permissionState.allPermissionsGranted) return@LaunchedEffect

        val activity = context as? Activity
        if (activity != null) {
            val shouldShowRationale = permissionState.permissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    it.permission
                )
            }

            if (shouldShowRationale) {
                showRationale = true
            } else {
                permissionState.launchMultiplePermissionRequest()
            }
        } else {
            // Fallback si le contexte n'est pas une Activity
            permissionState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            onPermissionsGranted()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (permissionState.allPermissionsGranted) {
            content()
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Demande de permissions...")
            }
        }

        if (showRationale) {
            AlertDialog(
                onDismissRequest = { showRationale = false },
                title = { Text("Permissions requises") },
                text = {
                    Text("L'application a besoin des permissions pour fonctionner correctement :" +
                            "\n- Lire/envoyer des SMS" +
                            "\n- Accéder aux contacts" +
                            "\n- État du téléphone")
                },
                confirmButton = {
                    Button(onClick = {
                        permissionState.launchMultiplePermissionRequest()
                        showRationale = false
                    }) {
                        Text("Compris")
                    }
                }
            )
        }
    }
}
