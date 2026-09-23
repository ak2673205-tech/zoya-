package com.example.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

class InstalledAppsManager(private val context: Context) {

    private val commonAliases = mapOf(
        "youtube" to listOf("com.google.android.youtube"),
        "yt" to listOf("com.google.android.youtube"),
        "whatsapp" to listOf("com.whatsapp", "com.whatsapp.w4b"),
        "chrome" to listOf("com.android.chrome"),
        "google" to listOf("com.google.android.googlequicksearchbox"),
        "camera" to listOf("com.google.android.GoogleCamera", "com.android.camera", "com.sec.android.app.camera"),
        "settings" to listOf("com.android.settings"),
        "calculator" to listOf("com.google.android.calculator", "com.android.calculator2", "com.sec.android.app.popupcalculator"),
        "calc" to listOf("com.google.android.calculator", "com.android.calculator2"),
        "hisab" to listOf("com.google.android.calculator", "com.android.calculator2"),
        "spotify" to listOf("com.spotify.music"),
        "music" to listOf("com.spotify.music", "com.google.android.apps.youtube.music"),
        "gaana" to listOf("com.spotify.music", "com.gaana"),
        "maps" to listOf("com.google.android.apps.maps"),
        "gmail" to listOf("com.google.android.gm"),
        "email" to listOf("com.google.android.gm", "com.android.email"),
        "mail" to listOf("com.google.android.gm"),
        "phone" to listOf("com.google.android.dialer", "com.android.dialer", "com.samsung.android.dialer"),
        "dialer" to listOf("com.google.android.dialer", "com.android.dialer"),
        "contacts" to listOf("com.google.android.contacts", "com.android.contacts"),
        "clock" to listOf("com.google.android.deskclock", "com.android.deskclock"),
        "alarm" to listOf("com.google.android.deskclock"),
        "calendar" to listOf("com.google.android.calendar", "com.android.calendar"),
        "gallery" to listOf("com.google.android.apps.photos", "com.android.gallery3d", "com.sec.android.gallery3d"),
        "photos" to listOf("com.google.android.apps.photos"),
        "play store" to listOf("com.android.vending"),
        "store" to listOf("com.android.vending"),
        "files" to listOf("com.google.android.documentsui", "com.android.documentsui", "com.google.android.apps.nbu.files")
    )

    fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }

        return resolveInfos.mapNotNull { info ->
            val pkg = info.activityInfo.packageName
            val label = info.loadLabel(pm).toString()
            if (pkg.isNotEmpty() && label.isNotEmpty()) {
                AppInfo(packageName = pkg, appName = label)
            } else null
        }.distinctBy { it.packageName }
    }

    fun findPackageForQuery(query: String): String? {
        val cleanQuery = query.lowercase().trim()

        // 1. Check known aliases first
        commonAliases[cleanQuery]?.forEach { pkgCandidate ->
            if (isPackageInstalled(pkgCandidate)) {
                return pkgCandidate
            }
        }

        // 2. Check installed apps for exact or partial name match
        val installed = getInstalledApps()
        val exactMatch = installed.find { it.appName.equals(cleanQuery, ignoreCase = true) }
        if (exactMatch != null) return exactMatch.packageName

        val containsMatch = installed.find {
            it.appName.contains(cleanQuery, ignoreCase = true) ||
                    cleanQuery.contains(it.appName, ignoreCase = true)
        }
        if (containsMatch != null) return containsMatch.packageName

        // 3. Fallback check common aliases if partial matches
        for ((alias, pkgs) in commonAliases) {
            if (cleanQuery.contains(alias) || alias.contains(cleanQuery)) {
                pkgs.forEach { pkgCandidate ->
                    if (isPackageInstalled(pkgCandidate)) {
                        return pkgCandidate
                    }
                }
            }
        }

        return null
    }

    fun isPackageInstalled(packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
