package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.voice.VoiceState

@Composable
fun WaveformVisualizer(
    voiceState: VoiceState,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 19
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
    ) {
        val isActive = voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING
        val isThinking = voiceState == VoiceState.THINKING

        for (i in 0 until barCount) {
            val offset = (i - barCount / 2f) / (barCount / 2f)
            val bellCurve = (1f - (offset * offset)).coerceAtLeast(0.15f)

            val barHeight = when {
                isActive -> {
                    val sine = (Math.sin(phase.toDouble() + (i * 0.45)).toFloat() + 1f) / 2f
                    (6.dp + (30.dp * bellCurve * (rmsLevel * 0.7f + sine * 0.3f))).coerceIn(4.dp, 36.dp)
                }
                isThinking -> {
                    val pulse = (Math.sin(phase.toDouble() * 2.0 + (i * 0.3)).toFloat() + 1f) / 2f
                    (6.dp + (18.dp * pulse)).coerceIn(4.dp, 28.dp)
                }
                else -> {
                    // Subtle idle heartbeat
                    val idleH = 4.dp + (4.dp * bellCurve)
                    idleH
                }
            }

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(ElectricCyan, NeonViolet)
                        )
                    )
            )
        }
    }
}
