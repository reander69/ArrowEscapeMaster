package com.arrowescape.master.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.arrowescape.master.model.ThemeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "arrow_escape_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_HIGHEST_LEVEL = intPreferencesKey("highest_unlocked_level")
        private val KEY_CLEARED_COUNT = intPreferencesKey("total_cleared_count")
        private val KEY_SELECTED_THEME = stringPreferencesKey("selected_theme")
        private val KEY_SOUND_ENABLED = booleanPreferencesKey("is_sound_enabled")
    }

    val highestUnlockedLevel: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_HIGHEST_LEVEL] ?: 1
    }

    val totalClearedCount: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_CLEARED_COUNT] ?: 0
    }

    val selectedTheme: Flow<ThemeType> = context.dataStore.data.map { prefs ->
        val themeName = prefs[KEY_SELECTED_THEME] ?: ThemeType.NEON_CYBER.name
        try {
            ThemeType.valueOf(themeName)
        } catch (e: Exception) {
            ThemeType.NEON_CYBER
        }
    }

    val isSoundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND_ENABLED] ?: true
    }

    fun getStarsForLevel(level: Int): Flow<Int> {
        val key = intPreferencesKey("level_stars_$level")
        return context.dataStore.data.map { prefs ->
            prefs[key] ?: 0
        }
    }

    suspend fun saveHighestUnlockedLevel(level: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_HIGHEST_LEVEL] ?: 1
            if (level > current) {
                prefs[KEY_HIGHEST_LEVEL] = level
            }
        }
    }

    suspend fun incrementClearedCount() {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_CLEARED_COUNT] ?: 0
            prefs[KEY_CLEARED_COUNT] = current + 1
        }
    }

    suspend fun saveTheme(theme: ThemeType) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_THEME] = theme.name
        }
    }

    suspend fun saveSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SOUND_ENABLED] = enabled
        }
    }

    suspend fun saveLevelStars(level: Int, stars: Int) {
        val key = intPreferencesKey("level_stars_$level")
        context.dataStore.edit { prefs ->
            val current = prefs[key] ?: 0
            if (stars > current) {
                prefs[key] = stars
            }
        }
    }
}
