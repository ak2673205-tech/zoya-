package com.example.ai

enum class IntentType {
    OPEN_APP,
    TOGGLE_TORCH,
    ADJUST_VOLUME,
    PHONE_CALL,
    WHATSAPP_MESSAGE,
    SEARCH_YOUTUBE,
    SEARCH_WEB,
    COMPOSE_EMAIL,
    OPEN_SETTINGS,
    BATTERY_STATUS,
    ACCESSIBILITY_CLICK,
    ACCESSIBILITY_TYPE,
    ACCESSIBILITY_SCROLL,
    ACCESSIBILITY_NAV,
    READ_SCREEN,
    GENERAL_CONVERSATION
}

data class ParsedIntent(
    val type: IntentType,
    val target: String? = null,
    val extra: String? = null,
    val originalQuery: String,
    val confidence: Float = 1.0f
)
