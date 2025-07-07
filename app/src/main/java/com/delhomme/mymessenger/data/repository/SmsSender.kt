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

    // Méthode pour envoyer un SMS simple
    fun sendSms(phoneNumber: String, message: String) {
        try {
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

    // Méthode pour envoyer un MMS avec média
    fun sendMms(phoneNumber: String, text: String, mediaUri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = context.contentResolver.getType(mediaUri) ?: "image/*"
                putExtra("address", phoneNumber)
                putExtra(Intent.EXTRA_STREAM, mediaUri)
                putExtra("sms_body", text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Envoyer le MMS"))
        } catch (e: Exception) {
            throw e
        }
    }

    // Méthode overload pour compatibilité avec votre code existant
    @Deprecated("Use sendSms(phoneNumber, message) or sendMms(phoneNumber, text, mediaUri)")
    fun sendSms(phoneNumber: String, message: String, mediaUri: Uri) {
        if (message.isNotBlank()) {
            sendSms(phoneNumber, message)
        }
        sendMms(phoneNumber, message, mediaUri)
    }
}