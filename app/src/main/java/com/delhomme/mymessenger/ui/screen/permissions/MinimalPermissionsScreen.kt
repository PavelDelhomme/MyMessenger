package com.delhomme.mymessenger.ui.screen.permissions

import android.Manifest
import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.delhomme.mymessenger.utils.requestDefaultSmsApp
import com.delhomme.mymessenger.utils.isDefaultSmsApp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MinimalPermissionsScreen(
    onAllGranted: () -> Unit,
    content: @Composable () -> Unit
) {
    // Demande des permissions critiques
    val criticalPermissions = listOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_MMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_PHONE_STATE
    )

    val permissionState = rememberMultiplePermissionsState(criticalPermissions)
    val context = LocalContext.current
    val activity = context as? Activity
    var isDefaultSmsApp by remember { mutableStateOf(isDefaultSmsApp(context)) }
    var isCheckingPermissions by remember { mutableStateOf(true) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Vérification continue du statut de l'application SMS par défaut
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                val currentStatus = isDefaultSmsApp(context)
                if (currentStatus != isDefaultSmsApp) {
                    isDefaultSmsApp = currentStatus
                }
                delay(1000)

                // Arrêter la vérification si on devient l'app par défaut
                if (isDefaultSmsApp) break
            }
        }
    }

    // Vérification initiale des permissions
    LaunchedEffect(Unit) {
        delay(500) // Petit délai pour l'initialisation
        isCheckingPermissions = false

        if (!permissionState.allPermissionsGranted) {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    when {
        isCheckingPermissions -> {
            // État de vérification initial
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(
                        text = "Vérification des permissions...",
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        !permissionState.allPermissionsGranted -> {
            // Permissions manquantes
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(
                                com.delhomme.mymessenger.R.drawable.ic_message
                            ),
                            contentDescription = "Permissions",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Permissions requises",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Text(
                            text = "L'application a besoin des permissions suivantes pour fonctionner :",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Column(
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            listOf(
                                "📱 Lire et envoyer des SMS",
                                "📧 Recevoir des SMS et MMS",
                                "👥 Accéder aux contacts",
                                "📞 État du téléphone"
                            ).forEach { permission ->
                                Text(
                                    text = permission,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                permissionState.launchMultiplePermissionRequest()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp)
                        ) {
                            Text("Accorder les permissions")
                        }
                    }
                }
            }
        }

        !isDefaultSmsApp -> {
            // Application SMS par défaut manquante
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(
                                com.delhomme.mymessenger.R.drawable.ic_sms
                            ),
                            contentDescription = "SMS App",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Application SMS par défaut",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Text(
                            text = "Pour recevoir et envoyer des SMS, cette application doit être définie comme application SMS par défaut.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Button(
                            onClick = {
                                activity?.let { requestDefaultSmsApp(it) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp)
                        ) {
                            Text("Définir comme application par défaut")
                        }

                        TextButton(
                            onClick = {
                                // Forcer la vérification
                                isDefaultSmsApp = isDefaultSmsApp(context)
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Vérifier à nouveau")
                        }
                    }
                }
            }
        }

        else -> {
            // Tout est en ordre, afficher le contenu
            onAllGranted()
            content()
        }
    }
}