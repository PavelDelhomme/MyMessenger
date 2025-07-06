package com.delhomme.mymessenger.ui.screen.permissions

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat


@Composable
fun WithPermission(
    permission: String,
    onAction: () -> Unit,
    onPermissionDenied: () -> Unit = {},
    content: @Composable (requestPermission: () -> Unit) -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                onAction()
            } else {
                onPermissionDenied()
            }
        }
    )

    val requestPermission = remember {
        {
            when (ContextCompat.checkSelfPermission(context, permission)) {
                PackageManager.PERMISSION_GRANTED -> {
                    // Permission déjà accordée
                    onAction()
                }
                else -> {
                    // Demander la permission
                    launcher.launch(permission)
                }
            }
        }
    }

    content(requestPermission)
}