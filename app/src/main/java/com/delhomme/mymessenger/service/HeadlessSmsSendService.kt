package com.delhomme.mymessenger.service


import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("HeadlessSmsSendService", "Service d'envoi SMS démarré")
        // TODO: gérer l'envoi SMS en arrière-plan si besoin
        return START_NOT_STICKY
    }
}
