package com.delhomme.mymessenger.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MessageEntity::class, ConversationEntity::class],
    version = 1,
    exportSchema = false // Ajoute ceci pour enlever le warning
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao

    companion object {
        fun build(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, "messenger_db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE INDEX IF NOT EXISTS idx_conv_lastDate ON conversations(lastDate)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS idx_msg_convId_date ON messages(conversationId,date DESC)")
                    }
                })
                .build()
    }
}
