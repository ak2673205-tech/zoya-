package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient(
    private var customApiKey: String? = null
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun updateApiKey(key: String?) {
        customApiKey = key
    }

    private fun getEffectiveApiKey(): String {
        val custom = customApiKey?.trim()
        if (!custom.isNullOrEmpty()) return custom

        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API key configure nahi hai. Settings me jakar apna Gemini API key set kijiye."
        }

        try {
            val root = JSONObject()

            // System instruction
            val systemInstruction = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", """
                            You are ANU, a friendly, smart, natural, confident, playful, slightly witty, casual, and expressive personal Android AI assistant.
                            You understand Hindi, Hinglish, and English fluently.
                            Reply naturally in the user's language (Hindi, Hinglish, or English).
                            Avoid robotic answers. Give crisp, punchy, spoken-friendly responses.
                            Do not use Markdown asterisks or bold text, speak in pure conversational sentences.
                            Separate normal conversation from device automation. If the user asks "Tell me about YouTube", answer conversationally. Do not ask to open it unless they explicitly say "open" or "kholo".
                        """.trimIndent())
                    })
                }
                put("parts", parts)
            }
            root.put("systemInstruction", systemInstruction)

            // Contents array
            val contents = JSONArray()
            // Add up to 4 recent turns
            conversationHistory.takeLast(4).forEach { (userQuery, anuReply) ->
                contents.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply { put(JSONObject().apply { put("text", userQuery) }) })
                })
                contents.put(JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().apply { put(JSONObject().apply { put("text", anuReply) }) })
                })
            }

            // Current prompt
            contents.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply { put(JSONObject().apply { put("text", prompt) }) })
            })
            root.put("contents", contents)

            // Generation config
            val genConfig = JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 256)
            }
            root.put("generationConfig", genConfig)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = root.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error (${response.code}): $responseBody")
                return@withContext if (response.code == 400 || response.code == 403) {
                    "API key invalid ya expired lag rahi hai. Please Settings check kijiye."
                } else {
                    "Thoda sa network issue ho gaya, ek baar dobara try kijiye."
                }
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                text.trim()
            } else {
                "Main samajh nahi payi, ek baar fir se boliye?"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini: ${e.message}")
            "Network connect nahi ho pa raha hai. Check your internet connection."
        }
    }

    companion object {
        private const val TAG = "GeminiClient"
    }
}
