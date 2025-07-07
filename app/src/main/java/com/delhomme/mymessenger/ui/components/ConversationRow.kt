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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.delhomme.mymessenger.R
import android.Manifest
import com.delhomme.mymessenger.data.local.ConversationEntity
import com.delhomme.mymessenger.ui.screen.permissions.WithPermission
import com.delhomme.mymessenger.utils.formatConversationDate
import com.delhomme.mymessenger.utils.formatFrenchPhoneNumber
import com.delhomme.mymessenger.utils.normalizePhoneNumber
import kotlin.math.roundToInt


@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun ConversationRow(
    conversation: ConversationEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onAction: (ConversationAction) -> Unit
) {
    val context = LocalContext.current

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onAction(ConversationAction.Archive)
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onAction(ConversationAction.Delete)
                    true
                }
                else -> false
            }
        }
    )

    val swipeState = rememberSwipeableState(initialValue = 0)
    val swipeAnchors = mapOf(0f to 0, -150f to -1, 150f to 1)
    val coroutineScope = rememberCoroutineScope()

    val background = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface


    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Color.Blue
                SwipeToDismissBoxValue.EndToStart -> Color.Red
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    painter = painterResource(
                        id = when (dismissState.dismissDirection) {
                            SwipeToDismissBoxValue.StartToEnd -> R.drawable.ic_archive
                            SwipeToDismissBoxValue.EndToStart -> R.drawable.ic_delete
                            else -> R.drawable.ic_person
                        }
                    ),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        },

        content = {
            // Ici on utilise WithPermission pour le contenu principal
            WithPermission(
                permission = Manifest.permission.READ_CONTACTS, // ← CORRECTION: android.Manifest
                onAction = { onClick() },
                onPermissionDenied = { onClick() }
            ) { requestPermission ->
                // Contenu principal de la ligne de conversation
                Row(
                    modifier = Modifier
                        .offset { IntOffset(swipeState.offset.value.roundToInt(), 0) }
                        .background(background)
                        .clickable { requestPermission() } // ← Le clic demande la permission si nécessaire
                        .padding(8.dp)
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContactAvatar(
                        name = conversation.fullName.takeIf { it.isNotBlank() },
                        photoUri = conversation.photoUri,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = conversation.fullName.ifBlank {
                                    normalizePhoneNumber(formatFrenchPhoneNumber(conversation.phoneNumber))
                                },
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
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

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatConversationDate(conversation.lastDate),
                            style = MaterialTheme.typography.labelSmall
                        )

                        if (conversation.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(20.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
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
    )
}

enum class ConversationAction {
    Archive, Unarchive, Mute, Unmute, Delete, Pin, Unpin, Block, Unblock, Call
}