package com.arrowescape.master.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arrowescape.master.model.GameState
import com.arrowescape.master.ui.theme.glassmorphicCard
import com.arrowescape.master.ui.theme.neonGlow
import kotlinx.coroutines.delay

@Composable
fun VictoryDialog(
    gameState: GameState,
    onNextLevelPressed: () -> Unit,
    onReplayPressed: () -> Unit,
    onLevelSelectPressed: () -> Unit
) {
    val theme = gameState.themeColors
    val stars = gameState.stars

    var animateStars by remember { mutableStateOf(false) }
    val entranceScale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        entranceScale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        delay(100)
        animateStars = true
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(entranceScale.value)
                    .neonGlow(glowColor = theme.accent)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                theme.backgroundEnd,
                                theme.backgroundStart
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .glassmorphicCard(
                        shape = RoundedCornerShape(32.dp),
                        backgroundColor = Color.Transparent,
                        borderColor = theme.arrowGlow,
                        borderWidth = 2.5.dp
                    )
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Level Cleared Header Badge
                    Box(
                        modifier = Modifier
                            .glassmorphicCard(
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = theme.arrowPrimary.copy(alpha = 0.2f),
                                borderColor = theme.arrowGlow
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LEVEL ${gameState.level} CLEARED",
                            color = theme.arrowGlow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Main Title
                    Text(
                        text = "VICTORY!",
                        color = theme.textPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Glowing Stars Row with staggered spring bounces
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            val isEarned = i <= stars
                            val scale by animateFloatAsState(
                                targetValue = if (animateStars && isEarned) 1.3f else 0.7f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioHighBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "star_scale_$i"
                            )

                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star $i",
                                tint = if (isEarned) Color(0xFFFACC15) else Color(0x26FFFFFF),
                                modifier = Modifier
                                    .size(54.dp)
                                    .scale(scale)
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Box (Moves & Time) with gradient card background
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassmorphicCard(
                                shape = RoundedCornerShape(20.dp),
                                backgroundColor = theme.cardBackground,
                                borderColor = theme.gridLine
                            )
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MOVES USED",
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${gameState.moveCount}",
                                color = theme.accent,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CLEAR TIME",
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val mins = gameState.timeElapsedSeconds / 60
                            val secs = gameState.timeElapsedSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                color = theme.arrowGlow,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Next Level Primary GameButton (Unified Theme)
                    GameButton(
                        text = "NEXT LEVEL",
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = onNextLevelPressed,
                        theme = theme
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Secondary Action Buttons using unified GlassIconButton style
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassIconButton(
                            icon = Icons.Default.Refresh,
                            label = "Replay",
                            onClick = onReplayPressed,
                            theme = theme
                        )

                        GlassIconButton(
                            icon = Icons.Default.Home,
                            label = "Levels",
                            onClick = onLevelSelectPressed,
                            theme = theme
                        )
                    }
                }
            }
        }
    }
}
