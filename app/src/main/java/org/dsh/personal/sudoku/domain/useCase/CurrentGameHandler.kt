package org.dsh.personal.sudoku.domain.useCase

import kotlinx.coroutines.flow.Flow
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.repository.SudokuRepository

class CurrentGameHandler (val repository: SudokuRepository) {
    suspend fun hasGame(): Boolean {
        return repository.hasGame()
    }
    suspend fun hasGameFlow(): Flow<Boolean> {
        return repository.hasGameFlow()
    }
    suspend fun saveGame(game: SudokuGameState) {
        repository.saveGame(game)
    }
    suspend fun loadGame(): SudokuGameState? {
        return repository.loadGame()
    }
}