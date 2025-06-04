package org.dsh.personal.sudoku.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.dsh.personal.sudoku.data.CurrentGameStorage.PreferencesKeys.SUDOKU_GAME_STATE_KEY
import org.dsh.personal.sudoku.domain.entity.SudokuGameState


class CurrentGameStorage(private val dataStore: DataStore<Preferences>) {

    // Define keys for your preferences
    private object PreferencesKeys {
        val SUDOKU_GAME_STATE_KEY = stringPreferencesKey("sudoku_game_state")
    }

    val currentGame: Flow<SudokuGameState?> = dataStore.data
        .map { preferences ->
            val jsonString = preferences[SUDOKU_GAME_STATE_KEY]
            if (jsonString != null) {
                try {
                    Json.decodeFromString<SudokuGameState>(jsonString)
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }
        }

    // Function to save the theme settings
    suspend fun saveThemeSettings(state: SudokuGameState) {
        dataStore.edit { preferences ->
            val jsonString = Json.encodeToString(state)
            preferences[SUDOKU_GAME_STATE_KEY] = jsonString
        }
    }

    suspend fun clearGameState() {
        dataStore.edit { preferences ->
            preferences.remove(SUDOKU_GAME_STATE_KEY)
        }
    }
}
