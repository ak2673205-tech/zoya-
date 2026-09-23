package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AnuOrb
import com.example.ui.components.ConversationTranscript
import com.example.ui.components.QuickActionChips
import com.example.ui.components.StatusBadge
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AnuViewModel
import com.example.voice.VoiceState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainAssistantScreen(
    viewModel: AnuViewModel,
    onRequestPermissions: () -> Unit
) {
    val voiceState by viewModel.voiceState.collectAsState()
    val rmsLevel by viewModel.rmsLevel.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val partialText by viewModel.partialSpokenText.collectAsState()
    val statusMessage by viewModel.lastStatusMessage.collectAsState()

    val showSettings by viewModel.showSettings.collectAsState()
    val showPermissionsGuide by viewModel.showPermissionsGuide.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var currentTime by remember { mutableStateOf("") }
    var batteryText by remember { mutableStateOf("") }

    // Update live clock & battery
    LaunchedEffect(Unit) {
        val clockFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        while (true) {
            currentTime = clockFormat.format(Date())
            val rawBattery = viewModel.actionExecutor.executeBattery().message
            batteryText = rawBattery.replace("Battery is at ", "").replace(".", "")
            delay(10000L)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = SpaceBlack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SpaceBlack,
                            Color(0xFF090D18),
                            Color(0xFF0D1224)
                        )
                    )
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                // Top App Bar
                TopStatusHeader(
                    currentTime = currentTime,
                    batteryText = batteryText,
                    onOpenPermissions = { viewModel.openPermissionsGuide() },
                    onOpenSettings = { viewModel.openSettings() }
                )

                // Assistant State & Orb Section
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatusBadge(
                            voiceState = voiceState,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )

                        // Central Animated Orb
                        AnuOrb(
                            voiceState = voiceState,
                            rmsLevel = rmsLevel,
                            onClick = { viewModel.onMicTapped() }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Waveform
                        WaveformVisualizer(
                            voiceState = voiceState,
                            rmsLevel = rmsLevel,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Live Speech Preview or Status
                        val displayText = if (partialText.isNotEmpty()) {
                            "\"$partialText\""
                        } else {
                            statusMessage
                        }

                        Text(
                            text = displayText,
                            color = if (partialText.isNotEmpty()) ElectricCyan else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (partialText.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .testTag("status_or_speech_text")
                        )
                    }
                }

                // Quick Action Suggestion Chips
                QuickActionChips(
                    onChipClicked = { command ->
                        viewModel.processInput(command)
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Conversation History Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark.copy(alpha = 0.6f))
                        .border(1.dp, SurfaceElevated, RoundedCornerShape(16.dp))
                ) {
                    ConversationTranscript(conversations = conversations)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Input Control Bar
                BottomInputBar(
                    textInput = textInput,
                    voiceState = voiceState,
                    onTextChanged = { textInput = it },
                    onSendClicked = {
                        if (textInput.isNotBlank()) {
                            viewModel.processInput(textInput)
                            textInput = ""
                        }
                    },
                    onMicClicked = { viewModel.onMicTapped() }
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Dialogs
            if (showSettings) {
                SettingsDialog(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeSettings() }
                )
            }

            if (showPermissionsGuide) {
                PermissionsDialog(
                    onRequestRuntimePermissions = onRequestPermissions,
                    onDismiss = { viewModel.closePermissionsGuide() }
                )
            }
        }
    }
}

@Composable
fun TopStatusHeader(
    currentTime: String,
    batteryText: String,
    onOpenPermissions: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Logo & Name
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(ElectricCyan, NeonViolet)
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = SpaceBlack,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "ANU",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "AI Voice Assistant",
                    color = ElectricCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Clock & Battery & Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (currentTime.isNotEmpty()) {
                Text(
                    text = currentTime,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            if (batteryText.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = batteryText,
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            IconButton(
                onClick = onOpenPermissions,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("permissions_header_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Permissions",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("settings_header_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun BottomInputBar(
    textInput: String,
    voiceState: VoiceState,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onMicClicked: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        OutlinedTextField(
            value = textInput,
            onValueChange = onTextChanged,
            placeholder = { Text("Ask ANU or type a command...", color = TextMuted, fontSize = 13.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSendClicked() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = SurfaceElevated,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = ElectricCyan
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("user_text_input")
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (textInput.isNotBlank()) {
            IconButton(
                onClick = onSendClicked,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan)
                    .testTag("send_command_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Command",
                    tint = SpaceBlack,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            val isListening = voiceState == VoiceState.LISTENING
            val isSpeaking = voiceState == VoiceState.SPEAKING

            FloatingActionButton(
                onClick = onMicClicked,
                containerColor = if (isListening) NeonGreen else if (isSpeaking) NeonViolet else ElectricCyan,
                contentColor = SpaceBlack,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("mic_fab_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else if (isSpeaking) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
