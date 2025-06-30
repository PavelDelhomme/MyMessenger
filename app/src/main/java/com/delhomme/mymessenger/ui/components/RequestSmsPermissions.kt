package com.delhomme.mymessenger.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext


@Composable
fun RequestSmsPermissions(content: @Composable () -> Unit) {
    var permissionsGranted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        permissionsGranted = perms.values.all { it }
    }

    LaunchedEffect(Unit) {
        launcher.launch(
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CONTACTS
            )
        )
    }

    // Affiche le contenu seulement si les permissions sont accordées
    if (permissionsGranted) {
        content()
    }
}