package com.arrowescape.master.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arrowescape.master.model.GameState
import com.arrowescape.master.model.Tier
import com.arrowescape.master.ui.theme.glassmorphicCard

@Composable
fun LevelSelectModal(
    gameState: GameState,
    onLevelSelected: (level: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val theme = gameState.themeColors
    val highestUnlocked = gameState.highestUnlockedLevel

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .glassmorphicCard(
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = theme.backgroundStart.copy(alpha = 0.95f),
                    borderColor = theme.gridLine,
                    borderWidth = 1.5.dp
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT LEVEL",
                        color = theme.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .glassmorphicCard(
                                shape = CircleShape,
                                backgroundColor = theme.cardBackground,
                                borderColor = theme.gridLine
                            )
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Level Grid (1 to 100)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(100) { index ->
                        val levelNum = index + 1
                        val isUnlocked = levelNum <= highestUnlocked
                        val isCurrent = levelNum == gameState.level
                        val tier = Tier.fromLevel(levelNum)

                        LevelTile(
                            levelNum = levelNum,
                            isUnlocked = isUnlocked,
                            isCurrent = isCurrent,
                            tier = tier,
                            theme = theme,
                            onClick = {
                                if (isUnlocked) {
                                    onLevelSelected(levelNum)
                                    onDismiss()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelTile(
    levelNum: Int,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    tier: Tier,
    theme: com.arrowescape.master.model.ThemeColors,
    onClick: () -> Unit
) {
    val bgColor = when {
        isCurrent -> theme.accent
        isUnlocked -> theme.cardBackground
        else -> Color(0x0DFFFFFF)
    }

    val borderColor = when {
        isCurrent -> Color.White
        isUnlocked -> Color(tier.hexColor)
        else -> Color(0x1AFFFFFF)
    }

    Box(
        modifier = Modifier
            .size(68.dp)
            .glassmorphicCard(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = bgColor,
                borderColor = borderColor,
                borderWidth = if (isCurrent) 2.5.dp else 1.dp
            )
            .clickable(enabled = isUnlocked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isUnlocked) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$levelNum",
                    color = if (isCurrent) Color.White else theme.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Star Indicators
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = Color(0xFFFACC15),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        } else {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color(0x40FFFFFF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
