package com.delhomme.mymessenger.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsSender @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun sendSms(phoneNumber: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                putExtra("address", phoneNumber)
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Envoyer le SMS"))
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            throw e // Propager l'exception pour la gestion d'erreur
        }
    }

    fun sendMms(context: Context, phoneNumber: String, text: String, mediaUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = context.contentResolver.getType(mediaUri) ?: "image/*"
            putExtra("address", phoneNumber)
            putExtra(Intent.EXTRA_STREAM, mediaUri)
            putExtra("sms_body", text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Envoyer le MMS"))
    }
}