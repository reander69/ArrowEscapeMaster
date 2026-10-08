package com.arrowescape.master.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrowescape.master.model.GameState
import com.arrowescape.master.ui.components.LevelSelectModal
import com.arrowescape.master.ui.theme.glassmorphicCard
import com.arrowescape.master.ui.theme.neonGlow

@Composable
fun MainMenuScreen(
    gameState: GameState,
    onStartGame: (level: Int) -> Unit,
    onCycleTheme: () -> Unit,
    onToggleSound: () -> Unit
) {
    val theme = gameState.themeColors
    var showLevelSelect by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(theme.backgroundStart, theme.backgroundEnd)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Game Title
            Text(
                text = "ARROW ESCAPE",
                color = theme.textPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "MASTER",
                color = theme.accent,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Highest Unlocked Level Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = theme.cardBackground,
                        borderColor = theme.gridLine
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HIGHEST LEVEL UNLOCKED",
                        color = theme.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "LEVEL ${gameState.highestUnlockedLevel}",
                        color = theme.arrowGlow,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Play Button (Thicker, bolder primary gradient button)
            CustomPrimaryButton(
                text = "PLAY NOW",
                icon = Icons.Default.PlayArrow,
                gradientColors = listOf(theme.arrowPrimary, theme.accent),
                glowColor = theme.arrowPrimary,
                onClick = { onStartGame(gameState.highestUnlockedLevel) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Level Select Button (Solid Opaque Frosted Glass Button)
            CustomSecondaryButton(
                text = "SELECT LEVEL",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                backgroundColor = theme.backgroundEnd,
                borderColor = theme.gridLine,
                textColor = theme.textPrimary,
                onClick = { showLevelSelect = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Optimized Bottom Controls Row (Theme & Sound Buttons centered and polished)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainMenuThemeButton(
                    gameState = gameState,
                    onClick = onCycleTheme
                )

                MainMenuSoundButton(
                    gameState = gameState,
                    onClick = onToggleSound
                )
            }
        }
    }

    if (showLevelSelect) {
        LevelSelectModal(
            gameState = gameState,
            onLevelSelected = { selectedLevel ->
                onStartGame(selectedLevel)
            },
            onDismiss = { showLevelSelect = false }
        )
    }
}

@Composable
private fun CustomPrimaryButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    glowColor: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "primary_btn_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(64.dp) // Thicker, more prominent touch target
            .scale(scale)
            .neonGlow(glowColor = glowColor)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(colors = gradientColors),
                shape = RoundedCornerShape(24.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
private fun CustomSecondaryButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "secondary_btn_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(54.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(18.dp)
            )
            .glassmorphicCard(
                shape = RoundedCornerShape(18.dp),
                backgroundColor = backgroundColor,
                borderColor = borderColor,
                borderWidth = 1.5.dp
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun MainMenuThemeButton(
    gameState: GameState,
    onClick: () -> Unit
) {
    val theme = gameState.themeColors
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1.0f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "theme_scale"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = theme.arrowPrimary,
        animationSpec = tween(300),
        label = "theme_border"
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
                .size(54.dp)
                .glassmorphicCard(
                    shape = CircleShape,
                    backgroundColor = theme.cardBackground,
                    borderColor = animatedBorderColor,
                    borderWidth = 2.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(28.dp)) {
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
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = gameState.currentTheme.displayName.split(" ").first(),
            color = theme.arrowPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MainMenuSoundButton(
    gameState: GameState,
    onClick: () -> Unit
) {
    val theme = gameState.themeColors
    val isEnabled = gameState.isSoundEnabled
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1.0f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "sound_scale"
    )

    val activeColor = if (isEnabled) theme.arrowGlow else theme.textSecondary.copy(alpha = 0.4f)

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
                .size(54.dp)
                .glassmorphicCard(
                    shape = CircleShape,
                    backgroundColor = if (isEnabled) theme.cardBackground else theme.cardBackground.copy(alpha = 0.4f),
                    borderColor = if (isEnabled) theme.arrowGlow else theme.gridLine.copy(alpha = 0.3f),
                    borderWidth = if (isEnabled) 2.dp else 1.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(28.dp)) {
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

                drawPath(
                    path = speakerPath,
                    color = activeColor
                )

                if (isEnabled) {
                    drawArc(
                        color = activeColor,
                        startAngle = -45f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(w * 0.30f, h * 0.25f),
                        size = androidx.compose.ui.geometry.Size(w * 0.50f, h * 0.50f),
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
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isEnabled) "Sound" else "Muted",
            color = if (isEnabled) theme.textPrimary else theme.textSecondary.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
