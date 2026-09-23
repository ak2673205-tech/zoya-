package com.example.ai

object IntentAnalyzer {

    fun analyze(rawQuery: String): ParsedIntent {
        val text = rawQuery.trim()
        val lower = text.lowercase()

        // 1. Check if user is asking informational questions ABOUT an app/feature rather than a command
        val isInfoQuestion = lower.startsWith("tell me about") ||
                lower.startsWith("what is") ||
                lower.startsWith("kya hai") ||
                lower.startsWith("who is") ||
                lower.startsWith("explain") ||
                lower.contains("ke baare me batao") ||
                lower.contains("kya hota hai")

        if (isInfoQuestion) {
            return ParsedIntent(
                type = IntentType.GENERAL_CONVERSATION,
                originalQuery = text
            )
        }

        // 2. Flashlight / Torch
        if (lower.contains("torch") || lower.contains("flashlight")) {
            val isOff = lower.contains("off") || lower.contains("band") || lower.contains("bujhao")
            val isOn = lower.contains("on") || lower.contains("chalao") || lower.contains("jalao")
            val target = if (isOff) "false" else if (isOn) "true" else "toggle"
            return ParsedIntent(IntentType.TOGGLE_TORCH, target = target, originalQuery = text)
        }

        // 3. Volume
        if (lower.contains("volume") || lower.contains("awaaz") || lower.contains("sound")) {
            if (lower.contains("badhao") || lower.contains("up") || lower.contains("increase") || lower.contains("tez")) {
                return ParsedIntent(IntentType.ADJUST_VOLUME, target = "up", originalQuery = text)
            }
            if (lower.contains("kam") || lower.contains("down") || lower.contains("decrease") || lower.contains("ghatao")) {
                return ParsedIntent(IntentType.ADJUST_VOLUME, target = "down", originalQuery = text)
            }
        }

        // 4. Battery
        if (lower.contains("battery") || lower.contains("charge") || lower.contains("charging")) {
            return ParsedIntent(IntentType.BATTERY_STATUS, originalQuery = text)
        }

        // 5. Screen reading & Accessibility inspection
        if (lower.contains("screen par kya hai") || lower.contains("screen read") || lower.contains("screen padho") ||
            lower == "screen dekho" || lower.contains("kya dikh raha hai")
        ) {
            return ParsedIntent(IntentType.READ_SCREEN, originalQuery = text)
        }

        // 6. Accessibility navigation
        if (lower == "back jao" || lower == "go back" || lower == "wapas jao" || lower == "back") {
            return ParsedIntent(IntentType.ACCESSIBILITY_NAV, target = "back", originalQuery = text)
        }
        if (lower == "home jao" || lower == "go home" || lower == "home screen" || lower == "home") {
            return ParsedIntent(IntentType.ACCESSIBILITY_NAV, target = "home", originalQuery = text)
        }
        if (lower.contains("recent apps") || lower.contains("recents") || lower.contains("recent app")) {
            return ParsedIntent(IntentType.ACCESSIBILITY_NAV, target = "recents", originalQuery = text)
        }

        // 7. Accessibility click
        if ((lower.contains("click") || lower.contains("dabao") || lower.contains("press")) &&
            (lower.contains("button") || lower.contains("screen") || lower.contains("par"))
        ) {
            val target = text
                .replace(Regex("(?i)(screen par|par|button|dabao|click|karo|press)"), "")
                .trim()
            if (target.isNotEmpty()) {
                return ParsedIntent(IntentType.ACCESSIBILITY_CLICK, target = target, originalQuery = text)
            }
        }

        // 8. Accessibility scroll
        if (lower.contains("scroll")) {
            val down = !lower.contains("up") && !lower.contains("upar")
            return ParsedIntent(IntentType.ACCESSIBILITY_SCROLL, target = if (down) "down" else "up", originalQuery = text)
        }

        // 9. Accessibility type
        if (lower.contains("type karo") || lower.contains("likho")) {
            val regex = Regex("(?i)(type karo|likho)(:?\\s*)(.*)")
            val match = regex.find(text)
            val toType = match?.groups?.get(3)?.value?.trim()
            if (!toType.isNullOrBlank() && !lower.contains("whatsapp") && !lower.contains("email")) {
                return ParsedIntent(IntentType.ACCESSIBILITY_TYPE, target = toType, originalQuery = text)
            }
        }

        // 10. WhatsApp Message
        if (lower.contains("whatsapp")) {
            val cleaned = text.replace(Regex("(?i)(whatsapp|karo|send|message|bhejo)"), " ").trim()
            val parts = cleaned.split(":", limit = 2)
            val contact = parts.getOrNull(0)?.trim()?.replace(Regex("(?i)(ko)"), "")?.trim()
            val message = parts.getOrNull(1)?.trim()
            return ParsedIntent(
                type = IntentType.WHATSAPP_MESSAGE,
                target = contact?.ifEmpty { null },
                extra = message?.ifEmpty { null },
                originalQuery = text
            )
        }

        // 11. Phone Call
        if (lower.contains("call karo") || lower.startsWith("call ") || lower.contains("phone lagao")) {
            val target = text
                .replace(Regex("(?i)(call karo|phone lagao|call|phone|ko)"), "")
                .trim()
            if (target.isNotEmpty()) {
                return ParsedIntent(IntentType.PHONE_CALL, target = target, originalQuery = text)
            }
        }

        // 12. YouTube Search
        if (lower.contains("youtube") && (lower.contains("search") || lower.contains("dhundo") || lower.contains("dekho") || lower.contains("play"))) {
            val query = text
                .replace(Regex("(?i)(youtube par|youtube pe|youtube|search karo|search|dhundo|play karo|play)"), "")
                .trim()
            if (query.isNotEmpty()) {
                return ParsedIntent(IntentType.SEARCH_YOUTUBE, target = query, originalQuery = text)
            }
        }

        // 13. Web Search
        if (lower.startsWith("search ") || lower.contains("google par") || lower.contains("search karo")) {
            val query = text
                .replace(Regex("(?i)(google par|search karo|search|google pe|dhundo)"), "")
                .trim()
            if (query.isNotEmpty()) {
                return ParsedIntent(IntentType.SEARCH_WEB, target = query, originalQuery = text)
            }
        }

        // 14. Email
        if (lower.contains("email") || lower.contains("mail")) {
            return ParsedIntent(IntentType.COMPOSE_EMAIL, originalQuery = text)
        }

        // 15. Settings
        if (lower.contains("setting") || lower.contains("settings")) {
            return ParsedIntent(IntentType.OPEN_SETTINGS, originalQuery = text)
        }

        // 16. App Launching ("YouTube kholo", "Chrome open karo", "Spotify chalao", etc.)
        val launchKeywords = listOf("kholo", "open", "launch", "chalao", "start")
        for (kw in launchKeywords) {
            if (lower.contains(kw)) {
                val appCandidate = text
                    .replace(Regex("(?i)\\b($kw|karo|app|application|please|anu)\\b"), "")
                    .trim()
                if (appCandidate.isNotEmpty() && appCandidate.length < 25) {
                    return ParsedIntent(IntentType.OPEN_APP, target = appCandidate, originalQuery = text)
                }
            }
        }

        // Default to General Conversation
        return ParsedIntent(IntentType.GENERAL_CONVERSATION, originalQuery = text)
    }
}
