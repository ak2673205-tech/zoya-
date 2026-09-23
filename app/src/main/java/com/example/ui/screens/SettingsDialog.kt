package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.accessibility.AnuAccessibilityService
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AnuViewModel

@Composable
fun SettingsDialog(
    viewModel: AnuViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var customKey by remember { mutableStateOf(viewModel.secureStorage.getCustomApiKey() ?: "") }
    var wakeWordOn by remember { mutableStateOf(viewModel.secureStorage.isWakeWordEnabled()) }
    var overlayOn by remember { mutableStateOf(viewModel.secureStorage.isOverlayEnabled()) }
    var bgVoiceOn by remember { mutableStateOf(viewModel.secureStorage.isBackgroundVoiceEnabled()) }
    var memoryOn by remember { mutableStateOf(viewModel.secureStorage.isMemoryEnabled()) }

    val accessibilityConnected by AnuAccessibilityService.isConnected.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .border(1.dp, NeonViolet.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ANU Assistant Settings",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                HorizontalDivider(color = SurfaceElevated, modifier = Modifier.padding(vertical = 12.dp))

                // Gemini API Key Section
                SettingSectionTitle(icon = Icons.Default.VpnKey, title = "Gemini AI API Key")
                Text(
                    text = "Configure custom API key or use project default injected via Secrets panel.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = customKey,
                    onValueChange = {
                        customKey = it
                        viewModel.saveApiKey(it)
                    },
                    placeholder = { Text("Enter Gemini API key (optional)", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = SurfaceElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = ElectricCyan
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Voice & Wake Word
                SettingSectionTitle(icon = Icons.Default.Mic, title = "Voice & Wake Word")
                SettingToggleRow(
                    title = "Wake Word (\"Hey Anu\" / \"Anu\")",
                    subtitle = "Respond to wake phrase",
                    checked = wakeWordOn,
                    onCheckedChange = {
                        wakeWordOn = it
                        viewModel.secureStorage.setWakeWordEnabled(it)
                    },
                    testTag = "toggle_wake_word"
                )

                SettingToggleRow(
                    title = "Background Voice Service",
                    subtitle = "Keep assistant alive in background with persistent notification",
                    checked = bgVoiceOn,
                    onCheckedChange = {
                        bgVoiceOn = it
                        viewModel.toggleBackgroundVoice(it)
                    },
                    testTag = "toggle_bg_voice"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Overlay & Accessibility
                SettingSectionTitle(icon = Icons.Default.Layers, title = "Floating Overlay & Automation")
                SettingToggleRow(
                    title = "Floating ANU Orb",
                    subtitle = "Show draggable bubble over other apps",
                    checked = overlayOn,
                    onCheckedChange = {
                        if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        } else {
                            overlayOn = it
                            viewModel.toggleOverlay(it)
                        }
                    },
                    testTag = "toggle_floating_orb"
                )

                // Accessibility Service
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Accessibility Automation",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val isConnected = accessibilityConnected || AnuAccessibilityService.isAccessibilitySettingsEnabled(context)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isConnected) NeonGreen.copy(alpha = 0.2f) else CoralRed.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isConnected) "CONNECTED" else "OFF",
                                    color = if (isConnected) NeonGreen else CoralRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Required for screen reading, clicking buttons & app automation.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                        modifier = Modifier.testTag("open_accessibility_settings_button")
                    ) {
                        Text("Configure", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Memory & Privacy
                SettingSectionTitle(icon = Icons.Default.Psychology, title = "Memory & Privacy")
                SettingToggleRow(
                    title = "Assistant Memory",
                    subtitle = "Remember preferences and past queries for better context",
                    checked = memoryOn,
                    onCheckedChange = {
                        memoryOn = it
                        viewModel.secureStorage.setMemoryEnabled(it)
                    },
                    testTag = "toggle_memory"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.clearAllConversations() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("clear_history_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.height(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear History", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearAllAssistantData() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_all_data_button")
                    ) {
                        Text("Reset All Data", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App Info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "ANU Assistant v2.0",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Natural Voice Intelligence & Android Device Automation.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSectionTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
        Icon(icon, contentDescription = null, tint = ElectricCyan, modifier = Modifier.height(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ElectricCyan,
                checkedTrackColor = ElectricCyan.copy(alpha = 0.4f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = SurfaceElevated
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
