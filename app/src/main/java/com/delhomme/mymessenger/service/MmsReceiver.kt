package com.delhomme.mymessenger.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager

class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // L'intent contient le MMS, mais il faut parser le contenu
        // Pour un vrai import, tu dois parser le nouveau MMS comme dans l'import initial
        // et l'insérer dans Room (tu peux utiliser un Worker comme pour les SMS)
        // Exemple :
        val request = OneTimeWorkRequestBuilder<MmsSaveWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}

