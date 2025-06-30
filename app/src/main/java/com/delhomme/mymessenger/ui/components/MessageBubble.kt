package com.delhomme.mymessenger.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.delhomme.mymessenger.R
import com.delhomme.mymessenger.data.local.MessageEntity

@Composable
fun MessageBubble(message: MessageEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(60.dp)
        ) {
            Surface(
                color = if (message.isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 2.dp
            ) {
                Text(
                    text = message.body,
                    modifier = Modifier.padding(12.dp),
                    color = if (message.isMe) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
            // Ajoute une icône d'envoi pour les messages non envoyés
            if (!message.isMe) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = "Envoyer",
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}
