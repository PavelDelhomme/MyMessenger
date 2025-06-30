package com.delhomme.mymessenger.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.delhomme.mymessenger.data.local.AppDatabase
import com.delhomme.mymessenger.data.local.MessageEntity
import com.delhomme.mymessenger.data.repository.MessageRepository

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages?.forEach { sms ->
                val workRequest = OneTimeWorkRequestBuilder<SmsSaveWorker>()
                    .setInputData(workDataOf(
                        "address" to sms.originatingAddress,
                        "body" to sms.messageBody
                    ))
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }
}
