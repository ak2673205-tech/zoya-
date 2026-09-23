package com.example.ai

import com.example.automation.ActionExecutor
import com.example.automation.ActionResult
import com.example.memory.ConversationMemory
import com.example.voice.TextToSpeechManager
import com.example.voice.VoiceState
import com.example.voice.VoiceStateManager

class CommandProcessor(
    private val geminiClient: GeminiClient,
    private val actionExecutor: ActionExecutor,
    private val conversationMemory: ConversationMemory,
    private val ttsManager: TextToSpeechManager,
    private val voiceStateManager: VoiceStateManager
) {

    suspend fun processUserCommand(
        input: String,
        onResponseReady: (String, Boolean) -> Unit // (spokenResponse, isActionSuccess)
    ) {
        val cleanInput = input.trim()
        if (cleanInput.isBlank()) return

        // 1. Record user input in memory
        conversationMemory.recordUserMessage(cleanInput)
        voiceStateManager.setState(VoiceState.THINKING)

        // 2. Analyze intent
        val intent = IntentAnalyzer.analyze(cleanInput)

        // 3. If it is an action, execute directly and truthfully report
        if (intent.type != IntentType.GENERAL_CONVERSATION) {
            val actionResult: ActionResult = when (intent.type) {
                IntentType.OPEN_APP -> actionExecutor.executeLaunchApp(intent.target ?: "")
                IntentType.TOGGLE_TORCH -> {
                    val enable = when (intent.target) {
                        "true" -> true
                        "false" -> false
                        else -> null
                    }
                    actionExecutor.executeTorch(enable)
                }
                IntentType.ADJUST_VOLUME -> actionExecutor.executeVolume(intent.target == "up")
                IntentType.BATTERY_STATUS -> actionExecutor.executeBattery()
                IntentType.READ_SCREEN -> actionExecutor.executeReadScreen()
                IntentType.ACCESSIBILITY_NAV -> {
                    when (intent.target) {
                        "home" -> actionExecutor.executeHome()
                        "recents" -> actionExecutor.executeHome() // or recents
                        else -> actionExecutor.executeBack()
                    }
                }
                IntentType.ACCESSIBILITY_CLICK -> actionExecutor.executeAccessibilityClick(intent.target ?: "")
                IntentType.ACCESSIBILITY_TYPE -> actionExecutor.executeAccessibilityType(intent.target ?: "")
                IntentType.ACCESSIBILITY_SCROLL -> actionExecutor.executeAccessibilityScroll(intent.target != "up")
                IntentType.PHONE_CALL -> actionExecutor.executeCall(intent.target ?: "")
                IntentType.WHATSAPP_MESSAGE -> actionExecutor.executeWhatsApp(intent.target, intent.extra)
                IntentType.SEARCH_YOUTUBE -> actionExecutor.executeYouTubeSearch(intent.target ?: "")
                IntentType.SEARCH_WEB -> actionExecutor.executeWebSearch(intent.target ?: "")
                IntentType.COMPOSE_EMAIL -> actionExecutor.executeEmail(null, null, null)
                IntentType.OPEN_SETTINGS -> actionExecutor.executeSettings(intent.target)
                IntentType.GENERAL_CONVERSATION -> ActionResult(false, "")
            }

            val status = if (actionResult.success) "SUCCESS" else "FAILED"
            conversationMemory.recordAnuResponse(
                response = actionResult.message,
                intentType = intent.type.name,
                actionStatus = status
            )

            voiceStateManager.setState(VoiceState.SPEAKING)
            ttsManager.speak(actionResult.message)
            onResponseReady(actionResult.message, actionResult.success)
            return
        }

        // 4. If general conversation, consult Gemini with recent context
        val recentHistory = conversationMemory.getRecentContext(6).map { it.sender to it.text }
        val aiReply = geminiClient.generateResponse(cleanInput, recentHistory)

        conversationMemory.recordAnuResponse(
            response = aiReply,
            intentType = "CONVERSATION",
            actionStatus = "SUCCESS"
        )

        voiceStateManager.setState(VoiceState.SPEAKING)
        ttsManager.speak(aiReply)
        onResponseReady(aiReply, true)
    }
}
