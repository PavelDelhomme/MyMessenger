package com.delhomme.mymessenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
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
    repliedMessage: MessageEntity? = null,
    searchQuery: String = "", // Nouveau paramètre pour la recherche
    attachments: List<AttachmentItem> = emptyList() // Support des pièces jointes
) {
    Box {
        // Affichage du message avec ou sans pièces jointes
        if (attachments.isNotEmpty()) {
            MessageWithAttachments(
                message = message.body,
                attachments = attachments,
                isMe = message.isMe,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            MessageBubble(
                message = message,
                onLongClick = onLongClick,
                repliedMessage = repliedMessage,
                searchQuery = searchQuery // Passer la requête de recherche
            )
        }

        // Menu contextuel
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

        // Overlay de sélection
        if (isSelected) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color(0x33009EF7))
            )
        }
    }
}