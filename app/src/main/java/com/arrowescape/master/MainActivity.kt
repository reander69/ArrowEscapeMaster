package com.arrowescape.master

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.arrowescape.master.ui.screens.GameScreen
import com.arrowescape.master.ui.screens.MainMenuScreen
import com.arrowescape.master.ui.theme.ArrowEscapeMasterTheme
import com.arrowescape.master.viewmodel.GameViewModel

enum class Screen {
    MAIN_MENU,
    GAME
}

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable Edge-to-Edge & Hide System Bars for Immersive Fullscreen Gameplay
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            ArrowEscapeMasterTheme {
                val gameState by viewModel.gameState.collectAsState()
                val particles by viewModel.particles.collectAsState()

                var currentScreen by remember { mutableStateOf(Screen.MAIN_MENU) }

                BackHandler(enabled = currentScreen == Screen.GAME) {
                    currentScreen = Screen.MAIN_MENU
                }

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (currentScreen) {
                        Screen.MAIN_MENU -> {
                            MainMenuScreen(
                                gameState = gameState,
                                onStartGame = { level ->
                                    viewModel.loadLevel(level)
                                    currentScreen = Screen.GAME
                                },
                                onCycleTheme = { viewModel.onCycleThemePressed() },
                                onToggleSound = { viewModel.onToggleSoundPressed() }
                            )
                        }
                        Screen.GAME -> {
                            GameScreen(
                                gameState = gameState,
                                particles = particles,
                                onTileTapped = { r, c -> viewModel.onTileTapped(r, c) },
                                onUndoPressed = { viewModel.onUndoPressed() },
                                onHintPressed = { viewModel.onHintPressed() },
                                onRestartPressed = { viewModel.onRestartPressed() },
                                onNextLevelPressed = { viewModel.onNextLevelPressed() },
                                onSelectLevel = { level -> viewModel.loadLevel(level) },
                                onThemePressed = { viewModel.onCycleThemePressed() },
                                onSoundPressed = { viewModel.onToggleSoundPressed() },
                                onBackToMenu = { currentScreen = Screen.MAIN_MENU }
                            )
                        }
                    }
                }
            }
        }
    }
}
