package com.delhomme.mymessenger.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.Executors


@Database(
    entities = [MessageEntity::class, ConversationEntity::class],
    version = 6,                 // incrémentez à chaque changement de schéma
    exportSchema = false         // inutile si vous ne conservez pas de schémas
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao

    companion object {
        fun build(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, "messenger_db")
                .setQueryExecutor(Executors.newFixedThreadPool(4))
                .fallbackToDestructiveMigration()   // ⚠︎ efface TOUT à chaque changement
                .build()
    }
}