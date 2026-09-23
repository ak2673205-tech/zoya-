package com.example.memory.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "user" or "anu"
    val text: String,
    val intentType: String = "GENERAL",
    val actionStatus: String = "NONE", // "SUCCESS", "FAILED", "NONE"
    val timestamp: Long = System.currentTimeMillis()
)
