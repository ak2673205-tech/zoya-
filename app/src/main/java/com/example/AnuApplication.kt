package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.memory.AnuDatabase
import com.example.memory.ConversationMemory

class AnuApplication : Application() {

    lateinit var database: AnuDatabase
        private set

    lateinit var memory: ConversationMemory
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AnuDatabase.getInstance(this)
        memory = ConversationMemory(database.conversationDao(), database.preferenceDao())
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_VOICE,
                "ANU Voice Assistant Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Running in the background for voice interaction and automation"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_VOICE = "anu_voice_channel"
        lateinit var instance: AnuApplication
            private set
    }
}
