package com.arrowescape.master.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import com.arrowescape.master.ui.theme.glassmorphicCard
import com.arrowescape.master.ui.theme.neonGlow

@Composable
fun LevelFailedDialog(
    gameState: GameState,
    onTryAgainPressed: () -> Unit
) {
    val theme = gameState.themeColors

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
                .background(Color(0xB3060B19)) // Smooth dark frosted scrim
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(
                        shape = RoundedCornerShape(28.dp),
                        backgroundColor = theme.backgroundEnd.copy(alpha = 0.95f),
                        borderColor = Color(0xFFEF4444),
                        borderWidth = 2.dp
                    )
                    .neonGlow(glowColor = Color(0xFFEF4444))
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LEVEL FAILED",
                        color = Color(0xFFEF4444),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "3 Wrong Attempts Reached!\nBoard is locked.",
                        color = theme.textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GameButton(
                        text = "TRY AGAIN",
                        icon = Icons.Default.Refresh,
                        onClick = onTryAgainPressed,
                        theme = theme
                    )
                }
            }
        }
    }
}
