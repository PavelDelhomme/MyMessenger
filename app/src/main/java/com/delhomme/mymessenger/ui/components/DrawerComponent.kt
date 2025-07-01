package com.delhomme.mymessenger.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DrawerComponent(
    navController: NavController,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Menu", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        listOf("Archive", "Corbeille", "Spam", "Paramètres").forEach { item ->
            Text(
                text = item,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Navigation selon l'option
                        when (item) {
                            "Archive" -> navController.navigate("archive")
                            "Corbeille" -> navController.navigate("trash")
                            "Spam" -> navController.navigate("spam")
                            "Paramètres" -> navController.navigate("settings")
                        }
                        onClose()
                    }
                    .padding(vertical = 12.dp)
            )
        }
    }
}