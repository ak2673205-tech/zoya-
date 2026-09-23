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
            val englishIndianLocale = Locale.forLanguageTag("en-IN")

            if (engine.isLanguageAvailable(hindiLocale) >= TextToSpeech.LANG_AVAILABLE) {
                engine.language = hindiLocale
            } else if (engine.isLanguageAvailable(englishIndianLocale) >= TextToSpeech.LANG_AVAILABLE) {
                engine.language = englishIndianLocale
            } else {
                engine.language = Locale.ENGLISH
            }

            // Select highest quality natural female voice for warm companion / GF mode tone
            try {
                val voices = engine.voices
                if (!voices.isNullOrEmpty()) {
                    val naturalVoice = voices.find { voice ->
                        val name = voice.name.lowercase(Locale.ROOT)
                        val isLangMatch = voice.locale.language == "hi" || (voice.locale.language == "en" && voice.locale.country == "IN")
                        val isFemale = name.contains("female") || name.contains("f00") || name.contains("-f-") ||
                                name.contains("hie") || name.contains("end") ||
                                voice.features.any { it.contains("female", ignoreCase = true) }
                        isLangMatch && isFemale && !voice.isNetworkConnectionRequired
                    } ?: voices.find { voice ->
                        val name = voice.name.lowercase(Locale.ROOT)
                        (name.contains("female") || voice.features.any { it.contains("female", ignoreCase = true) }) &&
                                !voice.isNetworkConnectionRequired
                    } ?: voices.find { voice ->
                        voice.locale.language == "hi" || (voice.locale.language == "en" && voice.locale.country == "IN")
                    }

                    naturalVoice?.let {
                        engine.voice = it
                        Log.d(TAG, "Selected natural companion voice: ${it.name}")
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Voice selection notice: ${e.message}")
            }

            // Sweet, feminine, warm companion tone (not robotic)
            engine.setPitch(1.14f)
            engine.setSpeechRate(0.98f)
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

        // Clean out any formatting, emojis, and symbols so speech sounds sweet and natural
        val cleanText = text
            .replace(Regex("[*#`_~>]"), "") // markdown
            .replace(Regex("[\uD800-\uDBFF][\uDC00-\uDFFF]"), "") // emojis
            .replace(Regex("[\\p{So}\\p{Cn}]"), "") // misc symbols
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleanText.isBlank()) return

        val result = tts?.speak(cleanText, queueMode, params, utteranceId)
        if (result == TextToSpeech.ERROR) {
            Log.w(TAG, "Notice speaking text: $cleanText")
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
