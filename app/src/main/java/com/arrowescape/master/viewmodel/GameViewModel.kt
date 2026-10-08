package com.arrowescape.master.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arrowescape.master.audio.HapticManager
import com.arrowescape.master.audio.HapticType
import com.arrowescape.master.audio.SoundEffect
import com.arrowescape.master.audio.SoundManager
import com.arrowescape.master.data.GameRepository
import com.arrowescape.master.data.PreferencesManager
import com.arrowescape.master.engine.LevelGenerator
import com.arrowescape.master.engine.PuzzleSolver
import com.arrowescape.master.model.FlyingArrow
import com.arrowescape.master.model.GameState
import com.arrowescape.master.model.GameStatus
import com.arrowescape.master.model.GridSnapshot
import com.arrowescape.master.model.GridTile
import com.arrowescape.master.ui.components.Particle
import com.arrowescape.master.ui.components.ParticleType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val repository = GameRepository(preferencesManager)
    val soundManager = SoundManager(application)
    val hapticManager = HapticManager(application)

    private val _gameState = MutableStateFlow(
        GameState(
            highestUnlockedLevel = repository.highestUnlockedLevel.value,
            totalClearedCount = repository.totalClearedCount.value,
            currentTheme = repository.selectedTheme.value,
            isSoundEnabled = repository.isSoundEnabled.value
        )
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    private var timerJob: Job? = null

    init {
        observePreferences()
        startParticleEngine()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            repository.selectedTheme.collect { theme ->
                _gameState.update { it.copy(currentTheme = theme) }
            }
        }

        viewModelScope.launch {
            repository.isSoundEnabled.collect { enabled ->
                soundManager.isMuted = !enabled
                _gameState.update { it.copy(isSoundEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            repository.highestUnlockedLevel.collect { highest ->
                _gameState.update { it.copy(highestUnlockedLevel = highest) }
            }
        }

        viewModelScope.launch {
            repository.totalClearedCount.collect { count ->
                _gameState.update { it.copy(totalClearedCount = count) }
            }
        }
    }

    fun loadLevel(levelNum: Int) {
        timerJob?.cancel()
        val grid = LevelGenerator.generateLevel(levelNum)
        val size = LevelGenerator.getGridSizeForLevel(levelNum)

        _gameState.update {
            it.copy(
                level = levelNum,
                rows = size.rows,
                cols = size.cols,
                grid = grid,
                flyingArrows = emptyList(),
                moveCount = 0,
                timeElapsedSeconds = 0,
                wrongAttemptsCount = 0,
                status = GameStatus.PLAYING,
                stars = 0,
                history = emptyList(),
                hintArrowId = null
            )
        }

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_gameState.value.status == GameStatus.PLAYING) {
                    _gameState.update { it.copy(timeElapsedSeconds = it.timeElapsedSeconds + 1) }
                }
            }
        }
    }

    fun onTileTapped(row: Int, col: Int) {
        val currentState = _gameState.value
        if (currentState.status != GameStatus.PLAYING) return

        val currentGrid = currentState.grid
        if (row !in 0 until currentState.rows || col !in 0 until currentState.cols) return

        val tile = currentGrid[row][col]
        clearHint()

        when (tile) {
            is GridTile.ArrowTile -> {
                if (PuzzleSolver.canArrowEscape(tile, currentGrid, currentState.rows, currentState.cols)) {
                    executeArrowEscape(tile)
                } else {
                    if (tile.isRotator) {
                        executeRotatorAction(tile)
                    } else if (tile.shieldCount > 0) {
                        executeShieldShatter(tile)
                    } else {
                        executeWrongAttempt()
                        executeWrongArrowPress(tile)
                    }
                }
            }
            is GridTile.Rock -> {
                executeWrongAttempt()
                executeWrongRockPress(row, col)
            }
            is GridTile.Empty -> {}
        }
    }

    private fun executeWrongAttempt() {
        val currentAttempts = _gameState.value.wrongAttemptsCount + 1
        if (currentAttempts >= 3) {
            timerJob?.cancel()
            _gameState.update {
                it.copy(
                    wrongAttemptsCount = currentAttempts,
                    status = GameStatus.LEVEL_FAILED
                )
            }
            soundManager.play(SoundEffect.BLOCKED_SHAKE)
            hapticManager.performHaptic(HapticType.BLOCKED_SHAKE, _gameState.value.isSoundEnabled)
        } else {
            _gameState.update { it.copy(wrongAttemptsCount = currentAttempts) }
        }
    }

    private fun saveSnapshot() {
        val current = _gameState.value
        val snapshot = GridSnapshot(
            grid = current.grid,
            moveCount = current.moveCount
        )
        _gameState.update { it.copy(history = it.history + snapshot) }
    }

    private fun executeArrowEscape(arrow: GridTile.ArrowTile) {
        soundManager.play(SoundEffect.SLIDE_LAUNCH)
        hapticManager.performHaptic(HapticType.LAUNCH_WHOOSH, _gameState.value.isSoundEnabled)
        saveSnapshot()

        spawnLaunchParticles(arrow)

        val updatedGrid = _gameState.value.grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, cell ->
                if (r == arrow.row && c == arrow.col) GridTile.Empty else cell
            }
        }

        val flyingArrow = FlyingArrow(
            id = arrow.id,
            row = arrow.row,
            col = arrow.col,
            direction = arrow.direction,
            isRotator = arrow.isRotator,
            shieldCount = arrow.shieldCount,
            progress = 0f
        )

        _gameState.update {
            it.copy(
                grid = updatedGrid,
                flyingArrows = it.flyingArrows + flyingArrow,
                moveCount = it.moveCount + 1
            )
        }

        viewModelScope.launch {
            val totalFrames = 22
            for (f in 1..totalFrames) {
                val progress = f.toFloat() / totalFrames
                _gameState.update { state ->
                    state.copy(
                        flyingArrows = state.flyingArrows.map { fa ->
                            if (fa.id == arrow.id) fa.copy(progress = progress) else fa
                        }
                    )
                }
                delay(10)
            }

            _gameState.update { state ->
                state.copy(
                    flyingArrows = state.flyingArrows.filter { it.id != arrow.id }
                )
            }

            checkWinCondition()
        }
    }

    private fun executeRotatorAction(arrow: GridTile.ArrowTile) {
        soundManager.play(SoundEffect.ROTATE)
        hapticManager.performHaptic(HapticType.ROTATION_CLICK, _gameState.value.isSoundEnabled)
        saveSnapshot()

        spawnRotatorParticles(arrow)

        viewModelScope.launch {
            val totalFrames = 12
            val newDirection = arrow.direction.rotateClockwise()

            for (f in 1..totalFrames) {
                val progress = f.toFloat() / totalFrames
                val rotDegrees = -90f * (1f - sin(progress * (Math.PI / 2.0)).toFloat())
                val scaleBounce = 1f + sin(progress * Math.PI.toFloat()) * 0.22f

                val rotatingTile = arrow.copyWithNewDirection(newDirection).copy(
                    rotationAnimDegrees = rotDegrees,
                    scale = scaleBounce
                )
                updateSingleTile(arrow.row, arrow.col, rotatingTile)
                delay(10)
            }

            val finalTile = arrow.copyWithNewDirection(newDirection).copy(
                rotationAnimDegrees = 0f,
                scale = 1f
            )
            updateSingleTile(arrow.row, arrow.col, finalTile)

            _gameState.update {
                it.copy(moveCount = it.moveCount + 1)
            }
        }
    }

    private fun executeShieldShatter(arrow: GridTile.ArrowTile) {
        soundManager.play(SoundEffect.SHIELD_CRACK)
        hapticManager.performHaptic(HapticType.SHIELD_CRACK, _gameState.value.isSoundEnabled)
        saveSnapshot()

        spawnShieldCrackParticles(arrow)

        viewModelScope.launch {
            val reducedArrow = arrow.copyWithReducedShield()
            updateSingleTile(arrow.row, arrow.col, reducedArrow.copy(scale = 1.35f))
            delay(50)
            updateSingleTile(arrow.row, arrow.col, reducedArrow.copy(scale = 0.92f))
            delay(40)
            updateSingleTile(arrow.row, arrow.col, reducedArrow.copy(scale = 1.0f))

            _gameState.update {
                it.copy(moveCount = it.moveCount + 1)
            }
        }
    }

    private fun executeWrongArrowPress(arrow: GridTile.ArrowTile) {
        soundManager.play(SoundEffect.WRONG_PRESS)
        hapticManager.performHaptic(HapticType.WRONG_PRESS, _gameState.value.isSoundEnabled)

        spawnWrongPressParticles(arrow.row, arrow.col)

        viewModelScope.launch {
            val totalFrames = 12
            for (f in 1..totalFrames) {
                val progress = f.toFloat() / totalFrames
                val shake = sin(progress * Math.PI.toFloat() * 6f) * (1f - progress) * 20.0f
                val shakingTile = arrow.copy(shakeOffset = shake, isShaking = true, scale = 1.03f)
                updateSingleTile(arrow.row, arrow.col, shakingTile)
                delay(10)
            }
            val normalTile = arrow.copy(shakeOffset = 0f, isShaking = false, scale = 1.0f)
            updateSingleTile(arrow.row, arrow.col, normalTile)
        }
    }

    private fun executeWrongRockPress(row: Int, col: Int) {
        soundManager.play(SoundEffect.WRONG_PRESS)
        hapticManager.performHaptic(HapticType.WRONG_PRESS, _gameState.value.isSoundEnabled)

        spawnWrongPressParticles(row, col)

        viewModelScope.launch {
            val tile = _gameState.value.grid.getOrNull(row)?.getOrNull(col) as? GridTile.Rock ?: return@launch
            val totalFrames = 12
            for (f in 1..totalFrames) {
                val progress = f.toFloat() / totalFrames
                val shake = sin(progress * Math.PI.toFloat() * 6f) * (1f - progress) * 16.0f
                updateSingleTile(row, col, tile.copy(shakeOffset = shake))
                delay(10)
            }
            updateSingleTile(row, col, tile.copy(shakeOffset = 0f))
        }
    }

    private fun updateSingleTile(row: Int, col: Int, newTile: GridTile) {
        val updatedGrid = _gameState.value.grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, cell ->
                if (r == row && c == col) newTile else cell
            }
        }
        _gameState.update { it.copy(grid = updatedGrid) }
    }

    private fun checkWinCondition() {
        if (_gameState.value.isBoardCleared) {
            timerJob?.cancel()
            val state = _gameState.value
            val remainingLives = (3 - state.wrongAttemptsCount).coerceIn(1, 3)
            val stars = remainingLives

            _gameState.update {
                it.copy(
                    status = GameStatus.VICTORY,
                    stars = stars
                )
            }

            soundManager.play(SoundEffect.VICTORY_CHIME)
            hapticManager.performHaptic(HapticType.VICTORY_PULSE, state.isSoundEnabled)

            viewModelScope.launch {
                repository.onLevelCompleted(state.level, stars)
            }
        }
    }

    fun onUndoPressed() {
        val state = _gameState.value
        if (state.history.isEmpty() || state.status != GameStatus.PLAYING) return

        soundManager.play(SoundEffect.UNDO)
        hapticManager.performHaptic(HapticType.LIGHT_TAP, state.isSoundEnabled)

        val lastSnapshot = state.history.last()
        _gameState.update {
            it.copy(
                grid = lastSnapshot.grid,
                flyingArrows = emptyList(),
                moveCount = lastSnapshot.moveCount,
                history = it.history.dropLast(1),
                hintArrowId = null
            )
        }
    }

    fun onHintPressed() {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING) return

        // Use intelligent PuzzleSolver.findNextHintTile supporting shields and rotators
        val hintTile = PuzzleSolver.findNextHintTile(state.grid, state.rows, state.cols)
        if (hintTile != null) {
            soundManager.play(SoundEffect.TAP)
            hapticManager.performHaptic(HapticType.LIGHT_TAP, state.isSoundEnabled)
            _gameState.update { it.copy(hintArrowId = hintTile.id) }
            spawnHintHighlightParticles(hintTile)
        }
    }

    private fun clearHint() {
        if (_gameState.value.hintArrowId != null) {
            _gameState.update { it.copy(hintArrowId = null) }
        }
    }

    fun onRestartPressed() {
        soundManager.play(SoundEffect.TAP)
        hapticManager.performHaptic(HapticType.LIGHT_TAP, _gameState.value.isSoundEnabled)
        loadLevel(_gameState.value.level)
    }

    fun onNextLevelPressed() {
        soundManager.play(SoundEffect.TAP)
        hapticManager.performHaptic(HapticType.LIGHT_TAP, _gameState.value.isSoundEnabled)
        loadLevel(_gameState.value.level + 1)
    }

    fun onCycleThemePressed() {
        soundManager.play(SoundEffect.TAP)
        hapticManager.performHaptic(HapticType.LIGHT_TAP, _gameState.value.isSoundEnabled)
        val nextTheme = _gameState.value.currentTheme.next()
        viewModelScope.launch {
            repository.saveTheme(nextTheme)
        }
    }

    fun onToggleSoundPressed() {
        soundManager.play(SoundEffect.TAP)
        hapticManager.performHaptic(HapticType.LIGHT_TAP, true)
        val newSoundState = !_gameState.value.isSoundEnabled
        viewModelScope.launch {
            repository.saveSoundEnabled(newSoundState)
        }
    }

    // Particle Generators
    private fun spawnLaunchParticles(arrow: GridTile.ArrowTile) {
        val theme = _gameState.value.themeColors
        val particlesList = mutableListOf<Particle>()

        for (i in 0 until 28) {
            particlesList.add(
                Particle(
                    x = arrow.col.toFloat() + 0.5f,
                    y = arrow.row.toFloat() + 0.5f,
                    vx = (Random.nextFloat() - 0.5f) * 0.22f + arrow.direction.dc * 0.16f,
                    vy = (Random.nextFloat() - 0.5f) * 0.22f + arrow.direction.dr * 0.16f,
                    color = theme.arrowGlow,
                    maxAge = 28,
                    size = Random.nextFloat() * 8f + 4f,
                    type = ParticleType.SPARK
                )
            )
        }

        _particles.update { it + particlesList }
    }

    private fun spawnRotatorParticles(arrow: GridTile.ArrowTile) {
        val theme = _gameState.value.themeColors
        val particlesList = mutableListOf<Particle>()

        for (i in 0 until 20) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            particlesList.add(
                Particle(
                    x = arrow.col.toFloat() + 0.5f,
                    y = arrow.row.toFloat() + 0.5f,
                    vx = Math.cos(angle.toDouble()).toFloat() * 0.14f,
                    vy = Math.sin(angle.toDouble()).toFloat() * 0.14f,
                    color = theme.rotatorPivot,
                    maxAge = 22,
                    size = 5f,
                    type = ParticleType.SPARK
                )
            )
        }

        _particles.update { it + particlesList }
    }

    private fun spawnShieldCrackParticles(arrow: GridTile.ArrowTile) {
        val theme = _gameState.value.themeColors
        val particlesList = mutableListOf<Particle>()

        for (i in 0 until 24) {
            particlesList.add(
                Particle(
                    x = arrow.col.toFloat() + 0.5f,
                    y = arrow.row.toFloat() + 0.5f,
                    vx = (Random.nextFloat() - 0.5f) * 0.2f,
                    vy = (Random.nextFloat() - 0.5f) * 0.2f,
                    color = theme.shieldArmor,
                    maxAge = 22,
                    size = Random.nextFloat() * 9f + 3f,
                    type = ParticleType.GLOW_DOT
                )
            )
        }

        _particles.update { it + particlesList }
    }

    private fun spawnWrongPressParticles(row: Int, col: Int) {
        val theme = _gameState.value.themeColors
        val particlesList = mutableListOf<Particle>()

        for (i in 0 until 16) {
            particlesList.add(
                Particle(
                    x = col.toFloat() + 0.5f,
                    y = row.toFloat() + 0.5f,
                    vx = (Random.nextFloat() - 0.5f) * 0.16f,
                    vy = (Random.nextFloat() - 0.5f) * 0.16f,
                    color = theme.rotatorPivot.copy(alpha = 0.9f),
                    maxAge = 20,
                    size = Random.nextFloat() * 6f + 3f,
                    type = ParticleType.SPARK
                )
            )
        }

        _particles.update { it + particlesList }
    }

    private fun spawnHintHighlightParticles(arrow: GridTile.ArrowTile) {
        val theme = _gameState.value.themeColors
        val particlesList = mutableListOf<Particle>()

        for (i in 0 until 22) {
            particlesList.add(
                Particle(
                    x = arrow.col.toFloat() + 0.5f,
                    y = arrow.row.toFloat() + 0.5f,
                    vx = (Random.nextFloat() - 0.5f) * 0.09f,
                    vy = (Random.nextFloat() - 0.5f) * 0.09f,
                    color = theme.accent,
                    maxAge = 30,
                    size = 6f,
                    type = ParticleType.GLOW_DOT
                )
            )
        }

        _particles.update { it + particlesList }
    }

    private fun startParticleEngine() {
        viewModelScope.launch {
            while (true) {
                delay(10)
                if (_particles.value.isNotEmpty()) {
                    _particles.update { list ->
                        list.mapNotNull { p ->
                            p.age++
                            p.vx *= 0.94f
                            p.vy *= 0.94f
                            p.x += p.vx
                            p.y += p.vy
                            if (p.age >= p.maxAge) null else p
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
