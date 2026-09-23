package com.example.automation

sealed class ActionStep {
    data class LaunchApp(val appName: String) : ActionStep()
    data class Delay(val millis: Long = 1000L) : ActionStep()
    data class Click(val target: String) : ActionStep()
    data class TypeText(val text: String) : ActionStep()
    data class Scroll(val down: Boolean = true) : ActionStep()
    object Back : ActionStep()
    object Home : ActionStep()
    data class SearchWeb(val query: String) : ActionStep()
}

data class ActionResult(
    val success: Boolean,
    val message: String,
    val details: String? = null
)
