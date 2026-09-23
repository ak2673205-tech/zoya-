package com.example.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

class MediaController(private val context: Context) {

    fun searchYouTube(query: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                // Browser fallback
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening YouTube: ${e.message}")
            false
        }
    }

    fun openSpotify(query: String? = null): Boolean {
        return try {
            val intent = if (!query.isNullOrBlank()) {
                Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:${Uri.encode(query)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                val launch = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launch
            }

            if (intent != null && intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                // Browser fallback or play store
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://open.spotify.com/search/${Uri.encode(query ?: "")}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Spotify: ${e.message}")
            false
        }
    }

    companion object {
        private const val TAG = "MediaController"
    }
}
