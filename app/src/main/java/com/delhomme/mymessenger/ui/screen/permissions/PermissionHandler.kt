package com.delhomme.mymessenger.ui.screen.permissions

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberPermissionLauncher(
    permission: String,
    onPermissionResult: (Boolean) -> Unit
): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onPermissionResult
    )

    return remember {
        {
            when (ContextCompat.checkSelfPermission(context, permission)) {
                PackageManager.PERMISSION_GRANTED -> {
                    // Permission déjà accordée, exécuter directement l'action
                    onPermissionResult(true)
                }
                else -> {
                    // Demander la permission
                    launcher.launch(permission)
                }
            }
        }
    }
}
