package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AnuApplication
import com.example.accessibility.SmartAccessibilityEngine
import com.example.ai.CommandProcessor
import com.example.ai.GeminiClient
import com.example.apps.AppLauncher
import com.example.apps.InstalledAppsManager
import com.example.automation.ActionExecutor
import com.example.automation.TaskExecutor
import com.example.memory.ConversationMemory
import com.example.memory.model.ConversationEntity
import com.example.screenvision.VisionDecisionEngine
import com.example.security.SecureStorage
import com.example.security.SecurityManager
import com.example.services.ForegroundVoiceService
import com.example.services.OverlayService
import com.example.tools.CommunicationController
import com.example.tools.DeviceController
import com.example.tools.EmailController
import com.example.tools.MediaController
import com.example.voice.SpeechRecognizerHelper
import com.example.voice.TextToSpeechManager
import com.example.voice.VoiceState
import com.example.voice.VoiceStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AnuViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AnuApplication
    val memory: ConversationMemory = app.memory
    val secureStorage = SecureStorage(application)
    val securityManager = SecurityManager(application)

    // Engines & Controllers
    private val appsManager = InstalledAppsManager(application)
    private val appLauncher = AppLauncher(application, appsManager)
    private val deviceController = DeviceController(application)
    private val commsController = CommunicationController(application)
    private val mediaController = MediaController(application)
    private val emailController = EmailController(application)
    val accessibilityEngine = SmartAccessibilityEngine(application)
    private val visionEngine = VisionDecisionEngine(accessibilityEngine)

    val actionExecutor = ActionExecutor(
        application,
        appLauncher,
        deviceController,
        commsController,
        mediaController,
        emailController,
        accessibilityEngine,
        visionEngine
    )
    val taskExecutor = TaskExecutor(actionExecutor)

    val voiceStateManager = VoiceStateManager(application)
    val voiceState: StateFlow<VoiceState> = voiceStateManager.voiceState
    val rmsLevel: StateFlow<Float> = voiceStateManager.rmsLevel

    private val geminiClient = GeminiClient(secureStorage.getCustomApiKey())

    private var ttsManager: TextToSpeechManager? = null
    private var speechRecognizerHelper: SpeechRecognizerHelper? = null

    private lateinit var commandProcessor: CommandProcessor

    val conversations: StateFlow<List<ConversationEntity>> = memory.conversationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _partialSpokenText = MutableStateFlow("")
    val partialSpokenText: StateFlow<String> = _partialSpokenText.asStateFlow()

    private val _lastStatusMessage = MutableStateFlow("Tap the orb or microphone to talk")
    val lastStatusMessage: StateFlow<String> = _lastStatusMessage.asStateFlow()

    // Dialog & UI states
    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _showPermissionsGuide = MutableStateFlow(false)
    val showPermissionsGuide: StateFlow<Boolean> = _showPermissionsGuide.asStateFlow()

    private val _confirmationAction = MutableStateFlow<Pair<String, () -> Unit>?>(null)
    val confirmationAction: StateFlow<Pair<String, () -> Unit>?> = _confirmationAction.asStateFlow()

    init {
        initVoiceAndAi()
    }

    private fun initVoiceAndAi() {
        ttsManager = TextToSpeechManager(
            context = getApplication(),
            onStartSpeaking = {
                voiceStateManager.setState(VoiceState.SPEAKING)
            },
            onDoneSpeaking = {
                voiceStateManager.setState(VoiceState.IDLE)
            },
            onErrorSpeaking = { error ->
                Log.e(TAG, "TTS Error: $error")
                voiceStateManager.setState(VoiceState.IDLE)
            }
        )

        speechRecognizerHelper = SpeechRecognizerHelper(
            context = getApplication(),
            onResult = { spokenText ->
                _partialSpokenText.value = ""
                processInput(spokenText)
            },
            onPartialResult = { partial ->
                _partialSpokenText.value = partial
            },
            onRmsChangedListener = { level ->
                voiceStateManager.updateRms(level)
            },
            onErrorListener = { errorMsg ->
                _partialSpokenText.value = ""
                voiceStateManager.setState(VoiceState.IDLE)
                if (errorMsg == "Listening stopped" || errorMsg == "Ready") {
                    _lastStatusMessage.value = "Tap the orb or microphone to talk"
                } else {
                    _lastStatusMessage.value = errorMsg
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(4000L)
                        if (_lastStatusMessage.value == errorMsg) {
                            _lastStatusMessage.value = "Tap the orb or microphone to talk"
                        }
                    }
                }
            }
        )

        commandProcessor = CommandProcessor(
            geminiClient = geminiClient,
            actionExecutor = actionExecutor,
            conversationMemory = memory,
            ttsManager = ttsManager!!,
            voiceStateManager = voiceStateManager
        )
    }

    fun onMicTapped() {
        // Interruption handling: if ANU is speaking, stop speaking immediately
        if (voiceState.value == VoiceState.SPEAKING) {
            ttsManager?.stop()
            voiceStateManager.setState(VoiceState.IDLE)
            return
        }

        if (voiceState.value == VoiceState.LISTENING) {
            speechRecognizerHelper?.stopListening()
            voiceStateManager.setState(VoiceState.IDLE)
            return
        }

        if (!securityManager.hasRecordAudioPermission()) {
            _showPermissionsGuide.value = true
            return
        }

        startListening()
    }

    private fun startListening() {
        ttsManager?.stop()
        voiceStateManager.setState(VoiceState.LISTENING)
        _lastStatusMessage.value = "Listening to you..."
        speechRecognizerHelper?.startListening("hi-IN")
    }

    fun processInput(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        // Interruption check
        ttsManager?.stop()

        _lastStatusMessage.value = "Processing command..."
        viewModelScope.launch {
            commandProcessor.processUserCommand(trimmed) { response, isSuccess ->
                _lastStatusMessage.value = response
            }
        }
    }

    fun openSettings() {
        _showSettings.value = true
    }

    fun closeSettings() {
        _showSettings.value = false
    }

    fun openPermissionsGuide() {
        _showPermissionsGuide.value = true
    }

    fun closePermissionsGuide() {
        _showPermissionsGuide.value = false
    }

    fun saveApiKey(key: String) {
        secureStorage.setCustomApiKey(key)
        geminiClient.updateApiKey(key)
    }

    fun toggleOverlay(enabled: Boolean) {
        secureStorage.setOverlayEnabled(enabled)
        if (enabled) {
            OverlayService.start(getApplication())
        } else {
            OverlayService.stop(getApplication())
        }
    }

    fun toggleBackgroundVoice(enabled: Boolean) {
        secureStorage.setBackgroundVoiceEnabled(enabled)
        if (enabled) {
            ForegroundVoiceService.start(getApplication())
        } else {
            ForegroundVoiceService.stop(getApplication())
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            memory.clearConversations()
            _lastStatusMessage.value = "Chat history cleared"
        }
    }

    fun clearAllAssistantData() {
        viewModelScope.launch {
            memory.clearAllData()
            secureStorage.setCustomApiKey(null)
            geminiClient.updateApiKey(null)
            _lastStatusMessage.value = "All assistant memory cleared"
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerHelper?.destroy()
        ttsManager?.shutdown()
    }

    companion object {
        private const val TAG = "AnuViewModel"
    }
}
