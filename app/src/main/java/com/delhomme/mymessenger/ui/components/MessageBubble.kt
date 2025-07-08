package com.delhomme.mymessenger.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.delhomme.mymessenger.R
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.utils.formatMessageDate

@Composable
fun MessageBubble(
    message: MessageEntity,
    onLongClick: () -> Unit,
    repliedMessage: MessageEntity? = null,
    searchQuery: String = "" // Nouveau paramètre pour la recherche
) {
    LaunchedEffect(message) {
        Log.d("MessageBubble", "Message: ${message.body}")
        Log.d("MessageBubble", "Status: ${message.status}")
    }

    val isMe = message.isMe
    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    val statusColor = when(message.status) {
        "SENDING" -> Color.Yellow
        "SENT" -> Color.Green
        "FAILED" -> Color.Red
        else -> textColor.copy(alpha = 0.7f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clickable { onLongClick() }
        ) {
            Surface(
                color = bubbleColor,
                shape = RoundedCornerShape(
                    topStart = if (isMe) 16.dp else 4.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Carte de réponse si message cité
                    repliedMessage?.let { replied ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Réponse à:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor.copy(alpha = 0.7f)
                                )

                                // Texte de la réponse avec surbrillance si recherché
                                if (searchQuery.isNotEmpty()) {
                                    HighlightedText(
                                        text = replied.body.take(50) + if (replied.body.length > 50) "..." else "",
                                        searchQuery = searchQuery,
                                        modifier = Modifier
                                    )
                                } else {
                                    Text(
                                        text = replied.body.take(50) + if (replied.body.length > 50) "..." else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textColor.copy(alpha = 0.8f),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }

                    // Texte principal du message avec surbrillance si recherché
                    if (searchQuery.isNotEmpty()) {
                        HighlightedText(
                            text = message.body,
                            searchQuery = searchQuery,
                            modifier = Modifier
                        )
                    } else {
                        Text(
                            text = message.body,
                            color = textColor
                        )
                    }

                    // Ligne de statut et heure
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatMessageDate(message.date),
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        if (isMe) {
                            Icon(
                                painter = painterResource(
                                    when(message.status) {
                                        "SENDING" -> R.drawable.ic_clock
                                        "SENT" -> R.drawable.ic_check
                                        "FAILED" -> R.drawable.ic_error
                                        else -> R.drawable.ic_clock
                                    }
                                ),
                                contentDescription = "Status",
                                tint = statusColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}