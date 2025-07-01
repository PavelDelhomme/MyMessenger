package com.delhomme.mymessenger.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.delhomme.mymessenger.domain.EnergySettings
import com.delhomme.mymessenger.domain.EnergySettings.spamCacheExpiry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var syncFrequency by remember { mutableStateOf(EnergySettings.syncFrequency) }
    var backgroundSync by remember { mutableStateOf(EnergySettings.backgroundSync) }
    var spamFilter by remember { mutableStateOf(EnergySettings.spamFilterEnabled) }
    var imageCompression by remember { mutableStateOf(EnergySettings.imageCompression) }
    var rcsEnabled by remember { mutableStateOf(EnergySettings.rcsEnabled) }
    var encryptionEnabled by remember { mutableStateOf(EnergySettings.encryptionEnabled) }

    var spamCacheSize by remember { mutableStateOf(EnergySettings.spamCacheSize) }
    var spamCacheExpiryHours by remember { mutableStateOf(EnergySettings.spamCacheExpiry / (60 * 60 * 1000).toInt()) } // en heures


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
        Text("Optimisation énergétique", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        Text("Fréquence de synchronisation", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = syncFrequency.toFloat(),
            onValueChange = { syncFrequency = it.toInt() },
            valueRange = 15f..240f,
            steps = 14
        )
        Text("$syncFrequency minutes")

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = backgroundSync,
                onCheckedChange = { backgroundSync = it }
            )
            Text("Synchronisation en arrière-plan")
        }


            Text("Messagerie avancée", style = MaterialTheme.typography.headlineMedium)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rcsEnabled,
                    onCheckedChange = { rcsEnabled = it }
                )
                Text("Activer RCS (Rich Communication Services)")
            }
            Text("Sécurité RCS", style = MaterialTheme.typography.headlineMedium)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rcsEnabled,
                    onCheckedChange = { rcsEnabled = it }
                )
                Text("Activer RCS")
            }
            if (rcsEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = encryptionEnabled,
                        onCheckedChange = { encryptionEnabled = it }
                    )
                    Text("Chiffrement end-to-end")
                }
            }

            Text("Taille du cache anti-spam", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = spamCacheSize.toFloat(),
                onValueChange = { spamCacheSize = it.toInt() },
                valueRange = 100f..5000f,
                steps = 49
            )
            Text("$spamCacheSize entrées")

            Spacer(Modifier.height(16.dp))

            Text("Expiration du cache (heures)", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = spamCacheExpiry.toFloat(),
                onValueChange = { spamCacheExpiry = it.toLong() },
                valueRange = 1f..720f, // 1 heure à 30 jours
                steps = 719
            )
            Text("$spamCacheExpiry heures")

            Button(
                onClick = {
                    EnergySettings.applySettings(
                        syncFrequency,
                        backgroundSync,
                        spamFilter,
                        imageCompression,
                        rcsEnabled,
                        spamCacheSize,
                        spamCacheExpiry,
                        encryptionEnabled
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("Enregistrer les paramètres")
            }
        }
    }
}

