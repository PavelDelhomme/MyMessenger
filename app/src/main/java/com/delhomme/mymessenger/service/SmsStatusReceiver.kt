package com.delhomme.mymessenger.service

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.delhomme.mymessenger.data.local.AppDatabase

class SmsStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getLongExtra("messageId", -1)
        val db = AppDatabase.build(context)
        val status = when (intent.action) {
            "SMS_SENT" -> if (resultCode == Activity.RESULT_OK) "SENT" else "FAILED"
            "SMS_DELIVERED" -> "DELIVERED"
            else -> "UNKNOWN"
        }
        if (messageId != -1L) {
            // Update status in Room
            db.messageDao().updateMessageStatus(messageId, status)
        }
    }
}
