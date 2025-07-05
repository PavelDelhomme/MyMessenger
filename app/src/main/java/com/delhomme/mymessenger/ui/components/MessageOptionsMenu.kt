package com.delhomme.mymessenger.ui.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

// MessageOptionsMenu.kt
@Composable
fun MessageOptionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onForward: () -> Unit,
    onReply: () -> Unit          // <- nouveau
) {
    DropdownMenu(expanded, onDismiss) {
        DropdownMenuItem({ Text("Répondre") }, onClick = { onReply(); onDismiss() })
        DropdownMenuItem({ Text("Copier")   }, onClick = { onCopy(); onDismiss() })
        DropdownMenuItem({ Text("Supprimer")}, onClick = { onDelete(); onDismiss() })
        DropdownMenuItem({ Text("Transférer")}, onClick = { onForward(); onDismiss() })
    }
}
