package com.delhomme.mymessenger

import android.app.Application
import com.delhomme.mymessenger.domain.EnergyOptimizer
import com.delhomme.mymessenger.domain.EnergySettings
import com.delhomme.mymessenger.service.SyncService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@HiltAndroidApp
class MessengerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (EnergySettings.backgroundSync) {
            EnergyOptimizer.scheduleTask(
                key = "message_sync",
                interval = EnergySettings.syncFrequency * 60 * 1000L,
                coroutineScope = CoroutineScope(Dispatchers.IO),
            ) {
                // synchronisation des messages si RCS est activé
                if (EnergySettings.rcsEnabled) {
                    SyncService.syncMessages(this)
                }
            }
        }
    }
}
