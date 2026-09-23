package com.example.apps

data class AppInfo(
    val packageName: String,
    val appName: String,
    val launchIntentAvailable: Boolean = true
)

sealed class AppLaunchResult {
    data class Success(val appName: String, val packageName: String) : AppLaunchResult()
    data class NotFound(val requestedName: String) : AppLaunchResult()
    data class Failed(val message: String) : AppLaunchResult()
}
