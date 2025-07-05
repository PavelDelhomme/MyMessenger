package com.delhomme.mymessenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onReply: () -> Unit,
    repliedMessage: MessageEntity? // <- le message cité (peut être null)
) {
    Box {
        MessageBubble(
            message = message,
            onLongClick = onLongClick,
            repliedMessage = repliedMessage
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
        if (isSelected) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color(0x33009EF7))
            )
        }
    }
}