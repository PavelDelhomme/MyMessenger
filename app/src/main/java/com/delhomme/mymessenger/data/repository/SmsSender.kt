package com.delhomme.mymessenger.data.repository

import android.content.Context
import android.telephony.SmsManager

object SmsSender {
    fun sendSms(context: Context, phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }
}