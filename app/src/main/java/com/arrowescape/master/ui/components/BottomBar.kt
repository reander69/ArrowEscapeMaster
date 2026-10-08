package com.arrowescape.master.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrowescape.master.model.GameState
import com.arrowescape.master.ui.theme.glassmorphicCard

@Composable
fun BottomBar(
    gameState: GameState,
    onUndoPressed: () -> Unit,
    onHintPressed: () -> Unit,
    onRestartPressed: () -> Unit,
    onThemePressed: () -> Unit,
    onSoundPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = gameState.themeColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Undo Button
        GlassIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            label = "Undo",
            enabled = gameState.history.isNotEmpty(),
            onClick = onUndoPressed,
            theme = theme
        )

        // 2. Hint Button
        GlassIconButton(
            icon = Icons.Default.Star,
            label = "Hint",
            onClick = onHintPressed,
            theme = theme
        )

        // 3. Restart Button
        GlassIconButton(
            icon = Icons.Default.Refresh,
            label = "Restart",
            onClick = onRestartPressed,
            theme = theme
        )

        // 4. Dynamic Theme Button matching unified glass theme
        CustomGlassIconButton(
            label = gameState.currentTheme.displayName.split(" ").first(),
            onClick = onThemePressed,
            theme = theme
        ) {
            Canvas(modifier = Modifier.size(26.dp)) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                drawCircle(
                    color = theme.arrowPrimary,
                    radius = radius * 0.85f,
                    center = center,
                    style = Stroke(width = 3.5f)
                )
                drawCircle(
                    color = theme.accent,
                    radius = radius * 0.40f,
                    center = center
                )
                drawCircle(
                    color = theme.arrowGlow,
                    radius = radius * 0.22f,
                    center = Offset(center.x + radius * 0.45f, center.y - radius * 0.45f)
                )
            }
        }

        // 5. Dynamic Sound Button matching unified glass theme
        val isSoundEnabled = gameState.isSoundEnabled
        CustomGlassIconButton(
            label = if (isSoundEnabled) "Sound" else "Muted",
            onClick = onSoundPressed,
            theme = theme
        ) {
            val activeColor = if (isSoundEnabled) theme.arrowGlow else theme.textSecondary.copy(alpha = 0.4f)
            Canvas(modifier = Modifier.size(26.dp)) {
                val w = size.width
                val h = size.height

                val speakerPath = Path().apply {
                    moveTo(w * 0.15f, h * 0.35f)
                    lineTo(w * 0.35f, h * 0.35f)
                    lineTo(w * 0.58f, h * 0.18f)
                    lineTo(w * 0.58f, h * 0.82f)
                    lineTo(w * 0.35f, h * 0.65f)
                    lineTo(w * 0.15f, h * 0.65f)
                    close()
                }

                drawPath(path = speakerPath, color = activeColor)

                if (isSoundEnabled) {
                    drawArc(
                        color = activeColor,
                        startAngle = -45f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(w * 0.30f, h * 0.25f),
                        size = Size(w * 0.50f, h * 0.50f),
                        style = Stroke(width = 2.5f)
                    )
                } else {
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = Offset(w * 0.20f, h * 0.20f),
                        end = Offset(w * 0.80f, h * 0.80f),
                        strokeWidth = 3f
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomGlassIconButton(
    label: String,
    onClick: () -> Unit,
    theme: com.arrowescape.master.model.ThemeColors,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1.0f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "custom_btn_scale"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = theme.gridLine,
        animationSpec = tween(300),
        label = "custom_border"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .glassmorphicCard(
                    shape = CircleShape,
                    backgroundColor = theme.cardBackground.copy(alpha = 0.85f),
                    borderColor = animatedBorderColor,
                    borderWidth = 1.5.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = theme.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
