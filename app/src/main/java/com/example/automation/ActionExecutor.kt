package com.example.automation

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.accessibility.SmartAccessibilityEngine
import com.example.apps.AppLaunchResult
import com.example.apps.AppLauncher
import com.example.screenvision.VisionDecisionEngine
import com.example.tools.CommunicationController
import com.example.tools.DeviceController
import com.example.tools.EmailController
import com.example.tools.MediaController

class ActionExecutor(
    private val context: Context,
    private val appLauncher: AppLauncher,
    private val deviceController: DeviceController,
    private val communicationController: CommunicationController,
    private val mediaController: MediaController,
    private val emailController: EmailController,
    private val accessibilityEngine: SmartAccessibilityEngine,
    private val visionEngine: VisionDecisionEngine
) {

    fun executeLaunchApp(appName: String): ActionResult {
        return when (val res = appLauncher.launchApp(appName)) {
            is AppLaunchResult.Success -> ActionResult(
                success = true,
                message = "Haanji, $appName open kar diya aapke liye!"
            )
            is AppLaunchResult.NotFound -> ActionResult(
                success = false,
                message = "Arey, ye app aapke phone me nahi mila."
            )
            is AppLaunchResult.Failed -> ActionResult(
                success = false,
                message = "$appName open nahi ho paya: ${res.message}"
            )
        }
    }

    fun executeTorch(enable: Boolean?): ActionResult {
        val ok = deviceController.toggleTorch(enable)
        return if (ok) {
            val stateText = if (enable == true) "on kar di" else if (enable == false) "off kar di" else "toggle kar di"
            ActionResult(true, "Aapke liye torch $stateText hai!")
        } else {
            ActionResult(false, "Torch on nahi ho saki.")
        }
    }

    fun executeVolume(increase: Boolean): ActionResult {
        val ok = deviceController.adjustVolume(increase)
        return if (ok) {
            ActionResult(true, if (increase) "Volume badha diya maine!" else "Volume dheere kar diya aapke liye!")
        } else {
            ActionResult(false, "Volume control nahi ho paya.")
        }
    }

    fun executeSettings(panel: String?): ActionResult {
        val ok = deviceController.openSettings(panel)
        return if (ok) {
            ActionResult(true, "Settings open kar di hai!")
        } else {
            ActionResult(false, "Settings open nahi ho saki.")
        }
    }

    fun executeBattery(): ActionResult {
        val status = deviceController.getBatteryStatus()
        return ActionResult(true, "Phone ki battery $status hai.")
    }

    fun executeCall(target: String): ActionResult {
        val ok = communicationController.makePhoneCall(target)
        return if (ok) {
            ActionResult(true, "Haanji, $target ko call laga rahi hoon!")
        } else {
            ActionResult(false, "$target ko call nahi lag saki. Permission check kijiye.")
        }
    }

    fun executeWhatsApp(contact: String?, message: String?): ActionResult {
        val ok = communicationController.openWhatsAppMessage(contact, message)
        return if (ok) {
            val dest = if (!contact.isNullOrBlank()) "$contact ke liye " else ""
            ActionResult(true, "${dest}WhatsApp open kar diya hai!")
        } else {
            ActionResult(false, "WhatsApp open nahi ho saka.")
        }
    }

    fun executeYouTubeSearch(query: String): ActionResult {
        val ok = mediaController.searchYouTube(query)
        return if (ok) {
            ActionResult(true, "Aapke liye YouTube par $query laga diya!")
        } else {
            ActionResult(false, "YouTube open nahi ho paya.")
        }
    }

    fun executeSpotify(query: String?): ActionResult {
        val ok = mediaController.openSpotify(query)
        return if (ok) {
            ActionResult(true, if (query.isNullOrBlank()) "Spotify open kar diya aapke liye!" else "Spotify par $query play kar rahi hoon!")
        } else {
            ActionResult(false, "Spotify open nahi ho paya.")
        }
    }

    fun executeEmail(recipient: String?, subject: String?, body: String?): ActionResult {
        val ok = emailController.composeEmail(recipient, subject, body)
        return if (ok) {
            ActionResult(true, "Email screen open kar di hai!")
        } else {
            ActionResult(false, "Email client open nahi ho paya.")
        }
    }

    fun executeWebSearch(query: String): ActionResult {
        return try {
            val uri = Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "Maine Google par $query search kar diya!")
        } catch (e: Exception) {
            ActionResult(false, "Browser open nahi ho paya.")
        }
    }

    fun executeAccessibilityClick(textOrId: String): ActionResult {
        if (!accessibilityEngine.isAvailable) {
            return ActionResult(false, "Is action ke liye Accessibility Service on kijiye.")
        }
        val ok = visionEngine.findAndClickMatchingElement(textOrId)
        return if (ok) {
            ActionResult(true, "$textOrId par click kar diya.")
        } else {
            ActionResult(false, "$textOrId screen par nahi mila.")
        }
    }

    fun executeAccessibilityType(text: String): ActionResult {
        if (!accessibilityEngine.isAvailable) {
            return ActionResult(false, "Typing ke liye Accessibility Service on kijiye.")
        }
        val ok = accessibilityEngine.type(text)
        return if (ok) {
            ActionResult(true, "\"$text\" type kar diya.")
        } else {
            ActionResult(false, "Focused text box nahi mila.")
        }
    }

    fun executeAccessibilityScroll(down: Boolean): ActionResult {
        if (!accessibilityEngine.isAvailable) {
            return ActionResult(false, "Scroll ke liye Accessibility Service on kijiye.")
        }
        val ok = if (down) accessibilityEngine.scrollDown() else accessibilityEngine.scrollUp()
        return if (ok) {
            ActionResult(true, if (down) "Neeche scroll kar diya." else "Upar scroll kar diya.")
        } else {
            ActionResult(false, "Scroll nahi ho paya.")
        }
    }

    fun executeBack(): ActionResult {
        val ok = accessibilityEngine.goBack()
        return if (ok) ActionResult(true, "Back chali gayi.")
        else ActionResult(false, "Back action ke liye Accessibility Service chahiye.")
    }

    fun executeHome(): ActionResult {
        val ok = accessibilityEngine.goHome()
        return if (ok) ActionResult(true, "Home screen par aa gayi.")
        else ActionResult(false, "Home action ke liye Accessibility Service chahiye.")
    }

    fun executeReadScreen(): ActionResult {
        val summary = visionEngine.summarizeCurrentScreen()
        return ActionResult(true, summary)
    }
}
