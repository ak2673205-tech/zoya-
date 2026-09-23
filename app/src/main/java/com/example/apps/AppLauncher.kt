package com.example.apps

import android.content.Context
import android.content.Intent
import android.util.Log

class AppLauncher(
    private val context: Context,
    private val appsManager: InstalledAppsManager
) {
    fun launchApp(appNameOrPackage: String): AppLaunchResult {
        val packageName = if (appNameOrPackage.contains(".")) {
            if (appsManager.isPackageInstalled(appNameOrPackage)) appNameOrPackage else null
        } else {
            appsManager.findPackageForQuery(appNameOrPackage)
        }

        if (packageName == null) {
            return AppLaunchResult.NotFound(appNameOrPackage)
        }

        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(launchIntent)
                AppLaunchResult.Success(appNameOrPackage, packageName)
            } else {
                AppLaunchResult.Failed("Cannot launch application $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: ${e.message}")
            AppLaunchResult.Failed(e.message ?: "Failed to launch app")
        }
    }

    companion object {
        private const val TAG = "AppLauncher"
    }
}
