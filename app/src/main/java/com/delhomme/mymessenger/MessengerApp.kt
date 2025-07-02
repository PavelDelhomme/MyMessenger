package com.delhomme.mymessenger

import android.app.Application
import com.delhomme.mymessenger.domain.EnergyOptimizer
import com.delhomme.mymessenger.domain.EnergySettings
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
                // pas de synchronisation car déjà gérer par SyncService
                //SyncService.startSync(this)
            }
        }
    }
}
