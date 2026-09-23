package com.example.screenvision

data class ScreenSummary(
    val titleOrHeader: String?,
    val interactiveItems: List<String>,
    val visibleTexts: List<String>
)

object ScreenHierarchyParser {

    fun parse(rawTexts: List<String>): ScreenSummary {
        val cleanList = rawTexts.map { it.trim() }.filter { it.isNotEmpty() }
        val header = cleanList.firstOrNull()
        val buttons = cleanList.filter { text ->
            val lower = text.lowercase()
            lower.contains("button") || lower.contains("search") || lower.contains("submit") ||
                    lower.contains("send") || lower.contains("ok") || lower.contains("cancel") ||
                    lower.contains("play") || lower.contains("pause")
        }

        return ScreenSummary(
            titleOrHeader = header,
            interactiveItems = buttons,
            visibleTexts = cleanList.take(20)
        )
    }
}
