package com.example.memory

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.memory.dao.ConversationDao
import com.example.memory.dao.PreferenceDao
import com.example.memory.model.AppPreferenceEntity
import com.example.memory.model.ConversationEntity

@Database(
    entities = [ConversationEntity::class, AppPreferenceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AnuDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun preferenceDao(): PreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: AnuDatabase? = null

        fun getInstance(context: Context): AnuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnuDatabase::class.java,
                    "anu_database.db"
                ).fallbackToDestructiveMigration(dropAllTables = false).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
