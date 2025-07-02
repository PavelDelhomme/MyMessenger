package com.delhomme.mymessenger.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.delhomme.mymessenger.data.local.MessageEntity

@Composable
fun MessageOptionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onForward: () -> Unit,
    onReply: () -> Unit
) {

    DropdownMenu(expanded, onDismiss) {
        DropdownMenuItem(text ={ Text("Répondre") }, onClick = { onReply(); onDismiss() } )
        DropdownMenuItem(text ={ Text("Copier") }, onClick = { onCopy(); onDismiss() } )
        DropdownMenuItem(text ={ Text("Supprimer") }, onClick = { onDelete(); onDismiss() } )
        DropdownMenuItem(text ={ Text("Transférer") }, onClick = { onForward(); onDismiss() } )    }
}