package com.example.memory

import com.example.memory.dao.ConversationDao
import com.example.memory.dao.PreferenceDao
import com.example.memory.model.AppPreferenceEntity
import com.example.memory.model.ConversationEntity
import kotlinx.coroutines.flow.Flow

class ConversationMemory(
    private val conversationDao: ConversationDao,
    private val preferenceDao: PreferenceDao
) {
    val conversationsFlow: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()

    suspend fun recordUserMessage(message: String): Long {
        return conversationDao.insertConversation(
            ConversationEntity(
                sender = "user",
                text = message,
                intentType = "USER_INPUT"
            )
        )
    }

    suspend fun recordAnuResponse(
        response: String,
        intentType: String = "GENERAL",
        actionStatus: String = "NONE"
    ): Long {
        return conversationDao.insertConversation(
            ConversationEntity(
                sender = "anu",
                text = response,
                intentType = intentType,
                actionStatus = actionStatus
            )
        )
    }

    suspend fun getRecentContext(limit: Int = 6): List<ConversationEntity> {
        return conversationDao.getRecentConversations(limit)
    }

    suspend fun clearConversations() {
        conversationDao.clearAll()
    }

    suspend fun setPreference(key: String, value: String) {
        preferenceDao.setPreference(AppPreferenceEntity(key, value))
    }

    fun getPreferenceFlow(key: String): Flow<String?> {
        return preferenceDao.getPreference(key)
    }

    suspend fun getPreference(key: String): String? {
        return preferenceDao.getPreferenceDirect(key)
    }

    suspend fun clearAllData() {
        conversationDao.clearAll()
        preferenceDao.clearAll()
    }
}
