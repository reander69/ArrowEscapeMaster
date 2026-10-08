package com.arrowescape.master.data

import android.content.Context
import android.content.SharedPreferences
import com.arrowescape.master.model.ThemeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("arrow_escape_game_prefs", Context.MODE_PRIVATE)

    private val _highestUnlockedLevel = MutableStateFlow(prefs.getInt(KEY_HIGHEST_LEVEL, 1))
    val highestUnlockedLevel: StateFlow<Int> = _highestUnlockedLevel.asStateFlow()

    private val _totalClearedCount = MutableStateFlow(prefs.getInt(KEY_CLEARED_COUNT, 0))
    val totalClearedCount: StateFlow<Int> = _totalClearedCount.asStateFlow()

    private val _selectedTheme = MutableStateFlow(
        try {
            ThemeType.valueOf(prefs.getString(KEY_SELECTED_THEME, ThemeType.NEON_CYBER.name) ?: ThemeType.NEON_CYBER.name)
        } catch (e: Exception) {
            ThemeType.NEON_CYBER
        }
    )
    val selectedTheme: StateFlow<ThemeType> = _selectedTheme.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND_ENABLED, true))
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    fun getStarsForLevel(level: Int): Int {
        return prefs.getInt("level_stars_$level", 0)
    }

    fun saveHighestUnlockedLevel(level: Int) {
        val current = prefs.getInt(KEY_HIGHEST_LEVEL, 1)
        if (level > current) {
            prefs.edit().putInt(KEY_HIGHEST_LEVEL, level).apply()
            _highestUnlockedLevel.value = level
        }
    }

    fun incrementClearedCount() {
        val current = prefs.getInt(KEY_CLEARED_COUNT, 0) + 1
        prefs.edit().putInt(KEY_CLEARED_COUNT, current).apply()
        _totalClearedCount.value = current
    }

    fun saveTheme(theme: ThemeType) {
        prefs.edit().putString(KEY_SELECTED_THEME, theme.name).apply()
        _selectedTheme.value = theme
    }

    fun saveSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        _isSoundEnabled.value = enabled
    }

    fun saveLevelStars(level: Int, stars: Int) {
        val key = "level_stars_$level"
        val current = prefs.getInt(key, 0)
        if (stars > current) {
            prefs.edit().putInt(key, stars).apply()
        }
    }

    companion object {
        private const val KEY_HIGHEST_LEVEL = "highest_unlocked_level"
        private const val KEY_CLEARED_COUNT = "total_cleared_count"
        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_SOUND_ENABLED = "is_sound_enabled"
    }
}
