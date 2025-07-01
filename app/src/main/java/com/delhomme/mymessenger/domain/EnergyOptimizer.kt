package com.delhomme.mymessenger.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object EnergyOptimizer {
    private val scheduledTasks = mutableMapOf<String, Job>()

    fun scheduleTask(
        key: String,
        interval: Long,
        coroutineScope: CoroutineScope,
        block: suspend () -> Unit
    ) {
        cancelTask(key)

        scheduledTasks[key] = coroutineScope.launch {
            while (isActive) {
                block()
                delay(interval)
            }
        }
    }

    fun cancelTask(key: String) {
        scheduledTasks[key]?.cancel()
        scheduledTasks.remove(key)
    }

    fun optimizeBackgroundTasks() {
        if (!EnergySettings.backgroundSync) {
            scheduledTasks.values.forEach { it.cancel() }
            scheduledTasks.clear()
        }
    }
}
