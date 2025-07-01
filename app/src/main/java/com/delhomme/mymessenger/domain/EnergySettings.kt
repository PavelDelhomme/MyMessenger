package com.delhomme.mymessenger.domain

object EnergySettings {
    var syncFrequency: Int = 60 // minutes
    var backgroundSync: Boolean = true
    var spamFilterEnabled: Boolean = true
    var imageCompression: Boolean = true
    var rcsEnabled: Boolean = false
    var spamCacheSize: Int = 1000 // Nombre d'entrées dans le cache
    var spamCacheExpiry: Long = 30L * 24 * 60 * 60 * 1000 // 30 jours
    var encryptionEnabled: Boolean = false

    fun applySettings(
        sync: Int,
        bgSync: Boolean,
        spam: Boolean,
        imgComp: Boolean,
        rcs: Boolean,
        spamCacheSize: Int,
        spamCacheExpiry: Long,
        encryptionEnabled: Boolean
    ) {
        syncFrequency = sync
        backgroundSync = bgSync
        spamFilterEnabled = spam
        imageCompression = imgComp
        rcsEnabled = rcs
        this.spamCacheSize = spamCacheSize
        this.spamCacheExpiry = spamCacheExpiry
        this.encryptionEnabled = encryptionEnabled
    }
}