package com.delhomme.mymessenger.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.*
import com.delhomme.mymessenger.data.repository.MessageRepository
import com.delhomme.mymessenger.utils.SmsSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SyncService : JobService() {

    @Inject
    lateinit var workManager: WorkManager // Injection via Hilt

    override fun onStartJob(params: JobParameters?): Boolean {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<SmsSyncWorker>(
            1, // Intervalle
            TimeUnit.HOURS // Unité
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "sms_sync",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }
}