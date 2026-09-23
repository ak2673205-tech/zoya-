package com.example.accessibility

import android.content.Context
import android.util.Log

class SmartAccessibilityEngine(private val context: Context) {

    private val service: AnuAccessibilityService?
        get() = AnuAccessibilityService.instance

    val isAvailable: Boolean
        get() = service != null

    fun isServiceConfiguredInSystem(): Boolean {
        return AnuAccessibilityService.isAccessibilitySettingsEnabled(context)
    }

    fun click(textOrId: String): Boolean {
        val s = service ?: run {
            Log.w(TAG, "AccessibilityService not running")
            return false
        }
        return s.clickElement(textOrId)
    }

    fun type(text: String): Boolean {
        val s = service ?: return false
        return s.typeText(text)
    }

    fun scrollDown(): Boolean {
        val s = service ?: return false
        return s.scroll(directionDown = true)
    }

    fun scrollUp(): Boolean {
        val s = service ?: return false
        return s.scroll(directionDown = false)
    }

    fun goBack(): Boolean {
        val s = service ?: return false
        return s.performBackAction()
    }

    fun goHome(): Boolean {
        val s = service ?: return false
        return s.performHomeAction()
    }

    fun openRecents(): Boolean {
        val s = service ?: return false
        return s.performRecentsAction()
    }

    fun inspectScreenTexts(): List<String> {
        val s = service ?: return emptyList()
        return s.dumpVisibleTexts()
    }

    companion object {
        private const val TAG = "SmartAccessibilityEng"
    }
}
