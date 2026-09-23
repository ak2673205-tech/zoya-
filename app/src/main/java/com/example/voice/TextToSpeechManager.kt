package com.example.voice

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.UUID

class TextToSpeechManager(
    private val context: Context,
    private val onStartSpeaking: () -> Unit = {},
    private val onDoneSpeaking: () -> Unit = {},
    private val onErrorSpeaking: (String) -> Unit = {}
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeech: String? = null

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                setupLanguage()
                setupAudioAttributes()
                setupProgressListener()

                pendingSpeech?.let {
                    speak(it)
                    pendingSpeech = null
                }
            } else {
                Log.e(TAG, "Failed to initialize TextToSpeech ($status)")
                onErrorSpeaking("Text-to-Speech initialization failed")
            }
        }
    }

    private fun setupLanguage() {
        tts?.let { engine ->
            val hindiLocale = Locale.forLanguageTag("hi-IN")
            val hindiResult = engine.isLanguageAvailable(hindiLocale)
            if (hindiResult >= TextToSpeech.LANG_AVAILABLE) {
                engine.language = hindiLocale
            } else {
                engine.language = Locale.ENGLISH
            }
            engine.setPitch(1.05f) // Friendly natural pitch
            engine.setSpeechRate(1.0f) // Natural speaking rate
        }
    }

    private fun setupAudioAttributes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            tts?.setAudioAttributes(attributes)
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onStartSpeaking()
            }

            override fun onDone(utteranceId: String?) {
                onDoneSpeaking()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onErrorSpeaking("TTS playback error")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                onErrorSpeaking("TTS error code: $errorCode")
            }
        })
    }

    fun speak(text: String, flush: Boolean = true) {
        if (text.isBlank()) return

        if (!isInitialized) {
            pendingSpeech = text
            return
        }

        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val utteranceId = UUID.randomUUID().toString()

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        // Clean out any markdown bolding / asterisks from AI text before speaking
        val cleanText = text
            .replace("**", "")
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .trim()

        val result = tts?.speak(cleanText, queueMode, params, utteranceId)
        if (result == TextToSpeech.ERROR) {
            Log.e(TAG, "Error speaking text: $cleanText")
            onErrorSpeaking("Failed to speak text")
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS: ${e.message}")
        }
        onDoneSpeaking()
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "TextToSpeechManager"
    }
}
