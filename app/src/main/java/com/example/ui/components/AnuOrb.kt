package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.PurpleGlow
import com.example.voice.VoiceState

@Composable
fun AnuOrb(
    voiceState: VoiceState,
    rmsLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_anim")

    // Slow continuous rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse for IDLE
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Fast pulse for THINKING
    val thinkingPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinking"
    )

    // Wave ring scale
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_scale"
    )

    val currentScale = when (voiceState) {
        VoiceState.IDLE -> breathingScale
        VoiceState.LISTENING -> 1.0f + (rmsLevel * 0.28f)
        VoiceState.THINKING -> thinkingPulse
        VoiceState.SPEAKING -> breathingScale * (1f + (rmsLevel * 0.22f))
        VoiceState.ERROR -> 0.95f
    }

    val primaryColor = when (voiceState) {
        VoiceState.IDLE -> ElectricCyan
        VoiceState.LISTENING -> CyanGlow
        VoiceState.THINKING -> PurpleGlow
        VoiceState.SPEAKING -> NeonViolet
        VoiceState.ERROR -> Color(0xFFF43F5E)
    }

    val secondaryColor = when (voiceState) {
        VoiceState.IDLE -> NeonViolet
        VoiceState.LISTENING -> NeonGreen
        VoiceState.THINKING -> ElectricCyan
        VoiceState.SPEAKING -> CyanGlow
        VoiceState.ERROR -> Color(0xFFFB923C)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(240.dp)
            .testTag("anu_central_orb")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.52f

            // Outer expanding wave in LISTENING and SPEAKING
            if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING) {
                val waveAlpha = (1f - (waveScale - 1f) / 0.45f).coerceIn(0f, 0.6f)
                drawCircle(
                    color = primaryColor.copy(alpha = waveAlpha),
                    radius = baseRadius * waveScale,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // Outer Orbit Glow Ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.8f),
                        secondaryColor.copy(alpha = 0.2f),
                        primaryColor.copy(alpha = 0.9f),
                        secondaryColor.copy(alpha = 0.4f),
                        primaryColor.copy(alpha = 0.8f)
                    ),
                    center = center
                ),
                radius = baseRadius * currentScale * 1.18f,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Middle Atmosphere Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f),
                        secondaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * currentScale * 1.35f
                ),
                radius = baseRadius * currentScale * 1.35f,
                center = center
            )

            // Inner AI Core Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        primaryColor,
                        secondaryColor.copy(alpha = 0.85f),
                        Color(0xFF0F172A)
                    ),
                    center = Offset(center.x - baseRadius * 0.2f, center.y - baseRadius * 0.2f),
                    radius = baseRadius * currentScale
                ),
                radius = baseRadius * currentScale,
                center = center
            )

            // Central White Spark
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = baseRadius * 0.18f * currentScale,
                center = Offset(center.x - baseRadius * 0.2f, center.y - baseRadius * 0.2f)
            )
        }
    }
}
