package com.example.screenvision

import com.example.accessibility.SmartAccessibilityEngine

class VisionDecisionEngine(private val accessibilityEngine: SmartAccessibilityEngine) {

    fun summarizeCurrentScreen(): String {
        if (!accessibilityEngine.isAvailable) {
            return "Screen dekhne ke liye Accessibility Service enable kijiye."
        }

        val rawTexts = accessibilityEngine.inspectScreenTexts()
        if (rawTexts.isEmpty()) {
            return "Screen par koi readable text nahi mila."
        }

        val summary = ScreenHierarchyParser.parse(rawTexts)
        val sb = StringBuilder()
        if (!summary.titleOrHeader.isNullOrBlank()) {
            sb.append("Current screen: ${summary.titleOrHeader}. ")
        }
        val preview = summary.visibleTexts.take(5).joinToString(", ")
        sb.append("Visible elements: $preview")
        return sb.toString()
    }

    fun findAndClickMatchingElement(userQuery: String): Boolean {
        if (!accessibilityEngine.isAvailable) return false

        val rawTexts = accessibilityEngine.inspectScreenTexts()
        val match = rawTexts.firstOrNull { it.contains(userQuery, ignoreCase = true) }
        return if (match != null) {
            accessibilityEngine.click(match)
        } else {
            accessibilityEngine.click(userQuery)
        }
    }
}
