package com.arrowescape.master.data

import com.arrowescape.master.model.ThemeType
import kotlinx.coroutines.flow.StateFlow

class GameRepository(private val preferencesManager: PreferencesManager) {

    val highestUnlockedLevel: StateFlow<Int> = preferencesManager.highestUnlockedLevel
    val totalClearedCount: StateFlow<Int> = preferencesManager.totalClearedCount
    val selectedTheme: StateFlow<ThemeType> = preferencesManager.selectedTheme
    val isSoundEnabled: StateFlow<Boolean> = preferencesManager.isSoundEnabled

    fun getStarsForLevel(level: Int): Int = preferencesManager.getStarsForLevel(level)

    suspend fun onLevelCompleted(level: Int, stars: Int) {
        preferencesManager.saveHighestUnlockedLevel(level + 1)
        preferencesManager.incrementClearedCount()
        preferencesManager.saveLevelStars(level, stars)
    }

    suspend fun saveTheme(theme: ThemeType) {
        preferencesManager.saveTheme(theme)
    }

    suspend fun saveSoundEnabled(enabled: Boolean) {
        preferencesManager.saveSoundEnabled(enabled)
    }
}
