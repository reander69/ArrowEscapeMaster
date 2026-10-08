package com.arrowescape.master.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.arrowescape.master.model.GameState
import com.arrowescape.master.model.GameStatus
import com.arrowescape.master.ui.components.BottomBar
import com.arrowescape.master.ui.components.GameCanvas
import com.arrowescape.master.ui.components.LevelFailedDialog
import com.arrowescape.master.ui.components.LevelSelectModal
import com.arrowescape.master.ui.components.Particle
import com.arrowescape.master.ui.components.TopHud
import com.arrowescape.master.ui.components.VictoryDialog

@Composable
fun GameScreen(
    gameState: GameState,
    particles: List<Particle>,
    onTileTapped: (row: Int, col: Int) -> Unit,
    onUndoPressed: () -> Unit,
    onHintPressed: () -> Unit,
    onRestartPressed: () -> Unit,
    onNextLevelPressed: () -> Unit,
    onSelectLevel: (level: Int) -> Unit,
    onThemePressed: () -> Unit,
    onSoundPressed: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val theme = gameState.themeColors
    var showLevelSelectModal by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(theme.backgroundStart, theme.backgroundEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top HUD
            TopHud(
                gameState = gameState,
                onMenuClicked = onBackToMenu
            )

            // 2. Interactive Game Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GameCanvas(
                    gameState = gameState,
                    particles = particles,
                    onTileTapped = onTileTapped
                )
            }

            // 3. Bottom Controls Bar
            BottomBar(
                gameState = gameState,
                onUndoPressed = onUndoPressed,
                onHintPressed = onHintPressed,
                onRestartPressed = onRestartPressed,
                onThemePressed = onThemePressed,
                onSoundPressed = onSoundPressed
            )
        }

        // 4. Victory Dialog Modal Overlay
        if (gameState.status == GameStatus.VICTORY) {
            VictoryDialog(
                gameState = gameState,
                onNextLevelPressed = onNextLevelPressed,
                onReplayPressed = onRestartPressed,
                onLevelSelectPressed = { showLevelSelectModal = true }
            )
        }

        // 5. Level Failed Dialog Modal Overlay (3 wrong attempts)
        if (gameState.status == GameStatus.LEVEL_FAILED) {
            LevelFailedDialog(
                gameState = gameState,
                onTryAgainPressed = onRestartPressed
            )
        }

        // 6. Level Select Modal Overlay
        if (showLevelSelectModal) {
            LevelSelectModal(
                gameState = gameState,
                onLevelSelected = { selectedLevel ->
                    showLevelSelectModal = false
                    onSelectLevel(selectedLevel)
                },
                onDismiss = { showLevelSelectModal = false }
            )
        }
    }
}
