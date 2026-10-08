package com.arrowescape.master.data

import com.arrowescape.master.model.ThemeType
import kotlinx.coroutines.flow.Flow

class GameRepository(private val preferencesManager: PreferencesManager) {

    val highestUnlockedLevel: Flow<Int> = preferencesManager.highestUnlockedLevel
    val totalClearedCount: Flow<Int> = preferencesManager.totalClearedCount
    val selectedTheme: Flow<ThemeType> = preferencesManager.selectedTheme
    val isSoundEnabled: Flow<Boolean> = preferencesManager.isSoundEnabled

    fun getStarsForLevel(level: Int): Flow<Int> = preferencesManager.getStarsForLevel(level)

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
