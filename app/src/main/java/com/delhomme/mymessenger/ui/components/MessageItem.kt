package com.delhomme.mymessenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.delhomme.mymessenger.data.local.MessageEntity

@Composable
fun MessageItem(
    message: MessageEntity,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    showMenu: Boolean,
    onMenuDismiss: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onForward: () -> Unit,
    onReply: () -> Unit
) {
    Box {
        MessageBubble(
            message = message,
            onLongClick = onLongClick
        )
        if (showMenu) {
            MessageOptionsMenu(
                expanded = showMenu,
                onDismiss = onMenuDismiss,
                onCopy = onCopy,
                onDelete = onDelete,
                onForward = onForward,
                onReply = onReply
            )
        }
        // Ajoute une indication visuelle si sélectionné (fond coloré, check, ...)
        if (isSelected) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color(0x33009EF7))
            )
        }
    }
}
