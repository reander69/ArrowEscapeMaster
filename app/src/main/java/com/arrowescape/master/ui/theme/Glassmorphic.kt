package com.arrowescape.master.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphicCard(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = Color(0x1F1E293B),
    borderColor: Color = Color(0x33FFFFFF),
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .border(borderWidth, borderColor, shape)

fun Modifier.neonGlow(
    glowColor: Color = Color(0xFF06B6D4),
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier = this
    .shadow(elevation = 12.dp, shape = shape, spotColor = glowColor, ambientColor = glowColor)

fun Modifier.animatedNeonBorder(
    primaryColor: Color = Color(0xFF06B6D4),
    secondaryColor: Color = Color(0xFFA855F7),
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "border_anim")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            primaryColor.copy(alpha = alpha),
            secondaryColor.copy(alpha = alpha)
        )
    )

    this
        .clip(shape)
        .border(1.5.dp, borderBrush, shape)
}
