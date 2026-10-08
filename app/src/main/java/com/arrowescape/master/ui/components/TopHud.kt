package com.arrowescape.master.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrowescape.master.model.GameState
import com.arrowescape.master.ui.theme.glassmorphicCard

@Composable
fun TopHud(
    gameState: GameState,
    onMenuClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tier = gameState.tier
    val theme = gameState.themeColors
    val remainingLives = (3 - gameState.wrongAttemptsCount).coerceAtLeast(0)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home Button using unified GlassIconButton style
        GlassIconButton(
            icon = Icons.Default.Home,
            label = "Menu",
            onClick = onMenuClicked,
            theme = theme
        )

        // Level & Tier Badge Container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LEVEL ${gameState.level}",
                color = theme.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .glassmorphicCard(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = Color(tier.hexColor).copy(alpha = 0.2f),
                        borderColor = Color(tier.hexColor)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tier.badgeSymbol,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = tier.title,
                    color = Color(tier.hexColor),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Lives Counter (Optimized Glowing Stars ⭐⭐⭐) & Moves/Timer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Optimized Stars Lives Display
            Row(
                modifier = Modifier
                    .glassmorphicCard(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = theme.cardBackground,
                        borderColor = Color(0xFFFACC15).copy(alpha = 0.6f)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                for (i in 1..3) {
                    val isActive = i <= remainingLives
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Life Star $i",
                        tint = if (isActive) Color(0xFFFACC15) else Color(0x33FFFFFF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Moves Counter
            Row(
                modifier = Modifier
                    .glassmorphicCard(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = theme.cardBackground,
                        borderColor = theme.gridLine
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Moves",
                    tint = theme.accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${gameState.moveCount}",
                    color = theme.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Timer Display
            Row(
                modifier = Modifier
                    .glassmorphicCard(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = theme.cardBackground,
                        borderColor = theme.gridLine
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Timer",
                    tint = theme.arrowGlow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val mins = gameState.timeElapsedSeconds / 60
                val secs = gameState.timeElapsedSeconds % 60
                Text(
                    text = String.format("%02d:%02d", mins, secs),
                    color = theme.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
