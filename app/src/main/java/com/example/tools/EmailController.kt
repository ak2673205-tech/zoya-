package com.example.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

class EmailController(private val context: Context) {

    fun composeEmail(recipient: String? = null, subject: String? = null, body: String? = null): Boolean {
        return try {
            val uri = Uri.parse("mailto:${recipient ?: ""}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                if (!subject.isNullOrBlank()) putExtra(Intent.EXTRA_SUBJECT, subject)
                if (!body.isNullOrBlank()) putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                // Try generic send
                val genericIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    if (!recipient.isNullOrBlank()) putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                    if (!subject.isNullOrBlank()) putExtra(Intent.EXTRA_SUBJECT, subject)
                    if (!body.isNullOrBlank()) putExtra(Intent.EXTRA_TEXT, body)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(genericIntent, "Send email").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error composing email: ${e.message}")
            false
        }
    }

    companion object {
        private const val TAG = "EmailController"
    }
}
