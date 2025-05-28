package org.dsh.personal.sudoku.presentation.game

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.dsh.personal.sudoku.domain.entity.SudokuBoardTheme
import org.dsh.personal.sudoku.domain.entity.SudokuEffects


class ThemeSettingsManager(private val context: Context) {

    private companion object {
        val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "sudoku_theme_settings")
    }

    // Define keys for your preferences
    private object PreferencesKeys {
        val USE_SYSTEM = booleanPreferencesKey("use_system_theme")
        val IS_DARK = booleanPreferencesKey("is_dark_theme")
        val IS_DYNAMIC = booleanPreferencesKey("is_dynamic_theme")
        val USE_HAPTIC = booleanPreferencesKey("use_haptic")
        val USE_SOUNDS = booleanPreferencesKey("use_sounds")
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
    }

    // Flow to read the theme settings
    val themeSettingsFlow: Flow<SudokuBoardTheme> = context.themeDataStore.data
        .map { preferences ->
            // Read the values from preferences, providing default values
            SudokuBoardTheme(
                useSystem = preferences[PreferencesKeys.USE_SYSTEM] != false,
                isDark = preferences[PreferencesKeys.IS_DARK] == true,
                isDynamic = preferences[PreferencesKeys.IS_DYNAMIC] != false
            )
        }

    // Function to save the theme settings
    suspend fun saveThemeSettings(theme: SudokuBoardTheme) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_SYSTEM] = theme.useSystem
            preferences[PreferencesKeys.IS_DARK] = theme.isDark
            preferences[PreferencesKeys.IS_DYNAMIC] = theme.isDynamic
        }
    }

    // Flow to read the theme settings
    val effectsFlow: Flow<SudokuEffects> = context.themeDataStore.data
        .map { preferences ->
            // Read the values from preferences, providing default values
            SudokuEffects(
                useHaptic = preferences[PreferencesKeys.USE_HAPTIC] != false,
                useSounds = preferences[PreferencesKeys.USE_SOUNDS] != false,
                soundVolume = preferences[PreferencesKeys.SOUND_VOLUME] ?: 6f
            )
        }

    // Function to save the theme settings
    suspend fun saveEffectsSettings(theme: SudokuEffects) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_HAPTIC] = theme.useHaptic
            preferences[PreferencesKeys.USE_SOUNDS] = theme.useSounds
            preferences[PreferencesKeys.SOUND_VOLUME] = theme.soundVolume
        }
    }
}