package com.example.security

import android.content.Context
import android.content.SharedPreferences

class SecureStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("anu_secure_settings", Context.MODE_PRIVATE)

    fun getCustomApiKey(): String? {
        return prefs.getString(KEY_API_KEY, null)
    }

    fun setCustomApiKey(key: String?) {
        prefs.edit().putString(KEY_API_KEY, key).apply()
    }

    fun isWakeWordEnabled(): Boolean {
        return prefs.getBoolean(KEY_WAKE_WORD, false)
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WAKE_WORD, enabled).apply()
    }

    fun isOverlayEnabled(): Boolean {
        return prefs.getBoolean(KEY_OVERLAY, false)
    }

    fun setOverlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY, enabled).apply()
    }

    fun isBackgroundVoiceEnabled(): Boolean {
        return prefs.getBoolean(KEY_BG_VOICE, false)
    }

    fun setBackgroundVoiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BG_VOICE, enabled).apply()
    }

    fun isMemoryEnabled(): Boolean {
        return prefs.getBoolean(KEY_MEMORY, true)
    }

    fun setMemoryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MEMORY, enabled).apply()
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "User") ?: "User"
    }

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    companion object {
        private const val KEY_API_KEY = "custom_gemini_api_key"
        private const val KEY_WAKE_WORD = "wake_word_enabled"
        private const val KEY_OVERLAY = "overlay_enabled"
        private const val KEY_BG_VOICE = "background_voice_enabled"
        private const val KEY_MEMORY = "memory_enabled"
        private const val KEY_USER_NAME = "user_name"
    }
}
