package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class SpeechRecognizerHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onPartialResult: (String) -> Unit = {},
    private val onRmsChangedListener: (Float) -> Unit = {},
    private val onErrorListener: (String) -> Unit = {}
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(TAG, "Ready for speech")
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d(TAG, "Beginning of speech")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize -2dB to ~10dB into 0f..1f
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        onRmsChangedListener(normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                        Log.d(TAG, "End of speech")
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        val errorMessage = getErrorText(error)
                        // Routine speech events (no match, timeout, client cancel) are not application errors
                        val isRoutineEvent = error == SpeechRecognizer.ERROR_NO_MATCH ||
                                error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
                                error == SpeechRecognizer.ERROR_CLIENT

                        if (isRoutineEvent) {
                            Log.d(TAG, "Speech recognition lifecycle event: $errorMessage (code: $error)")
                        } else {
                            Log.w(TAG, "Speech recognition non-fatal issue: $errorMessage (code: $error)")
                        }
                        onErrorListener(errorMessage)
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spokenText = matches?.firstOrNull()?.trim() ?: ""
                        if (spokenText.isNotEmpty()) {
                            onResult(spokenText)
                        } else {
                            onErrorListener("Koi aawaz nahi aayi")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotEmpty()) {
                            onPartialResult(text)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } else {
            Log.w(TAG, "SpeechRecognizer not available on device")
        }
    }

    fun startListening(languageLocale: String = "hi-IN") {
        if (speechRecognizer == null) {
            initRecognizer()
        }

        if (isListening) {
            cancel()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageLocale)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageLocale)
            // Multi-language support via string extras across Android versions
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN", "en-US"))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            // Allow reasonable pause length before completing speech
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1400L)
        }

        try {
            isListening = true
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Notice starting speech listening: ${e.message}")
            isListening = false
            onErrorListener("Mic start issue")
        }
    }

    fun stopListening() {
        if (isListening) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.w(TAG, "Notice stopping speech listening: ${e.message}")
            }
            isListening = false
        }
    }

    fun cancel() {
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.w(TAG, "Notice cancelling speech recognizer: ${e.message}")
        }
        isListening = false
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.w(TAG, "Notice destroying speech recognizer: ${e.message}")
        }
        isListening = false
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording issue"
            SpeechRecognizer.ERROR_CLIENT -> "Listening stopped"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network issue"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out"
            SpeechRecognizer.ERROR_NO_MATCH -> "Kuch sunaai nahi diya, dubara boliye"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Mic is busy"
            SpeechRecognizer.ERROR_SERVER -> "Server unavailable"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Koi aawaz nahi aayi"
            else -> "Ready"
        }
    }

    companion object {
        private const val TAG = "SpeechRecognizerHelper"
    }
}
