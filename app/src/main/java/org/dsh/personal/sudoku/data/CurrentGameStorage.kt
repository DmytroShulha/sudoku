package org.dsh.personal.sudoku.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dsh.personal.sudoku.data.CurrentGameStorage.PreferencesKeys.SUDOKU_GAME_STATE_KEY
import org.dsh.personal.sudoku.domain.entity.SudokuGameState


class CurrentGameStorage(private val context: Context) {

    private companion object {
        private val Context.currentGame: DataStore<Preferences> by preferencesDataStore(name = "sudoku_current_game")
    }


    // Define keys for your preferences
    private object PreferencesKeys {
        val SUDOKU_GAME_STATE_KEY = stringPreferencesKey("sudoku_game_state")
    }

    val currentGame: Flow<SudokuGameState?> = context.currentGame.data
        .map { preferences ->
            val jsonString = preferences[SUDOKU_GAME_STATE_KEY]
            if (jsonString != null) {
                try {
                    Json.decodeFromString<SudokuGameState>(jsonString)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            } else {
                null
            }
        }

    // Function to save the theme settings
    suspend fun saveThemeSettings(state: SudokuGameState) {
        context.currentGame.edit { preferences ->
            val jsonString = Json.encodeToString(state)
            preferences[SUDOKU_GAME_STATE_KEY] = jsonString
        }
    }

    suspend fun clearGameState() {
        context.currentGame.edit { preferences ->
            preferences.remove(SUDOKU_GAME_STATE_KEY)
        }
    }
}