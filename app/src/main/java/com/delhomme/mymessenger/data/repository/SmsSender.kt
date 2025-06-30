package com.delhomme.mymessenger.data.repository

import android.content.Context
import android.os.Build
import android.telephony.SmsManager

object SmsSender {
    fun sendSms(context: Context, phoneNumber: String, message: String) {
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
}
