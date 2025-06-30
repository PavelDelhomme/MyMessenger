package com.delhomme.mymessenger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MessageEntity::class, ConversationEntity::class],
    version = 1,
    exportSchema = false // Ajoute ceci pour enlever le warning
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
}
