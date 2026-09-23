package com.example.tools

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat

class CommunicationController(private val context: Context) {

    fun findContactNumber(contactName: String): Pair<String, String>? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "READ_CONTACTS permission not granted")
            return null
        }

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$contactName%")

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (nameIndex >= 0 && numberIndex >= 0) {
                    val name = cursor.getString(nameIndex)
                    val number = cursor.getString(numberIndex)
                    return Pair(name, number)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up contact: ${e.message}")
        } finally {
            cursor?.close()
        }
        return null
    }

    fun makePhoneCall(numberOrName: String): Boolean {
        val resolved = if (numberOrName.any { it.isLetter() }) {
            findContactNumber(numberOrName)?.second ?: numberOrName
        } else {
            numberOrName
        }

        val cleanNumber = resolved.replace(Regex("[^0-9+]"), "")

        return try {
            val hasCallPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED

            val intent = if (hasCallPermission) {
                Intent(Intent.ACTION_CALL, Uri.parse("tel:$cleanNumber")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error placing phone call: ${e.message}")
            false
        }
    }

    fun openWhatsAppMessage(contactOrNumber: String? = null, message: String? = null): Boolean {
        return try {
            val encodedMsg = Uri.encode(message ?: "")
            val intent = if (!contactOrNumber.isNullOrBlank()) {
                val phoneNumber = if (contactOrNumber.any { it.isLetter() }) {
                    findContactNumber(contactOrNumber)?.second ?: ""
                } else {
                    contactOrNumber
                }
                val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
                if (cleanNumber.isNotEmpty()) {
                    val url = "https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedMsg"
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        setPackage("com.whatsapp")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                } else {
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage("com.whatsapp")
                        putExtra(Intent.EXTRA_TEXT, message ?: "")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
            } else {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage("com.whatsapp")
                    putExtra(Intent.EXTRA_TEXT, message ?: "")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            // Fallback without package if whatsapp isn't set
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?text=$encodedMsg")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening WhatsApp: ${e.message}")
            false
        }
    }

    companion object {
        private const val TAG = "CommunicationController"
    }
}
