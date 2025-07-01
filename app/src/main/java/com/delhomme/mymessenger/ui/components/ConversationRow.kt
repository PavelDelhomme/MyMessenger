package com.delhomme.mymessenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.delhomme.mymessenger.R
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.utils.formatConversationDate
import com.delhomme.mymessenger.utils.formatFrenchPhoneNumber
import kotlin.math.roundToInt


@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun ConversationRow(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onAction: (ConversationAction) -> Unit
) {
    val swipeState = rememberSwipeableState(initialValue = 0)
    val swipeAnchors = mapOf(
        0f to 0,
        -100f to -1,
        100f to 1
    )

    Box(
        modifier = Modifier
            .swipeable(
                state = swipeState,
                anchors = swipeAnchors,
                thresholds = { _, _ -> FractionalThreshold(0.25f) },
                orientation = Orientation.Horizontal
            )
    ) {
        // Arrière plan des actions
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Actions gauches
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(150.dp)
                    .background(Color.Blue)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onAction(ConversationAction.Archive) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_archive),
                            contentDescription = "Archiver",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { onAction(ConversationAction.Pin) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_pin),
                            contentDescription = "Épingler",
                            tint = Color.White
                        )
                    }
                }
            }

            // Actions droites
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(150.dp)
                    .background(Color.Red)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onAction(ConversationAction.Mute) }) {
                        Icon(
                            painter = painterResource(R.drawable.notification_off),
                            contentDescription = "Rendre muet",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { onAction(ConversationAction.Delete) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = "Supprimer",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Contenu principal
        Row(
            modifier = Modifier
                .offset { IntOffset(swipeState.offset.value.roundToInt(), 0) }
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onClick() }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(
                name = conversation.fullName.takeIf { it.isNotBlank() },
                photoUri = conversation.photoUri,
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.fullName.ifBlank { formatFrenchPhoneNumber(conversation.phoneNumber) },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (conversation.isPinned) {
                        Icon(
                            painter = painterResource(R.drawable.ic_pin),
                            contentDescription = "Épinglé",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(start = 4.dp)
                        )
                    }
                }

                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatConversationDate(conversation.lastDate),
                    style = MaterialTheme.typography.labelSmall
                )

                if (conversation.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conversation.unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

enum class ConversationAction {
    Archive, Unarchive, Mute, Unmute, Delete, Pin, Unpin, Block, Unblock, Call
}