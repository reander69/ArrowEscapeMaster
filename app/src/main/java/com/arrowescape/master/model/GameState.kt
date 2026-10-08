package com.arrowescape.master.model

enum class GameStatus {
    PLAYING,
    VICTORY,
    LEVEL_FAILED,
    PAUSED
}

data class GameState(
    val level: Int = 1,
    val rows: Int = 3,
    val cols: Int = 3,
    val grid: List<List<GridTile>> = emptyList(),
    val flyingArrows: List<FlyingArrow> = emptyList(),
    val moveCount: Int = 0,
    val timeElapsedSeconds: Int = 0,
    val wrongAttemptsCount: Int = 0,
    val status: GameStatus = GameStatus.PLAYING,
    val stars: Int = 0,
    val history: List<GridSnapshot> = emptyList(),
    val hintArrowId: String? = null,
    val currentTheme: ThemeType = ThemeType.NEON_CYBER,
    val isSoundEnabled: Boolean = true,
    val highestUnlockedLevel: Int = 1,
    val totalClearedCount: Int = 0
) {
    val tier: Tier get() = Tier.fromLevel(level)

    val remainingArrowsCount: Int
        get() = grid.sumOf { row ->
            row.count { it is GridTile.ArrowTile }
        } + flyingArrows.size

    val isBoardCleared: Boolean
        get() = remainingArrowsCount == 0

    val themeColors: ThemeColors
        get() = ThemePalettes.get(currentTheme)
}
