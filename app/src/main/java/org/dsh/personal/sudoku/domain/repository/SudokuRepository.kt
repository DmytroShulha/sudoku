package org.dsh.personal.sudoku.domain.repository

import kotlinx.coroutines.flow.Flow
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.entity.SudokuGameStats

interface SudokuRepository {
    //Generation
    suspend fun generateGrid(generator: SudokuGenerator, difficulty: Difficulty): Pair<Array<IntArray>, Array<IntArray>>

    //Preferences
    suspend fun hasGame(): Boolean
    fun hasGameFlow(): Flow<Boolean>
    suspend fun saveGame(game: SudokuGameState)
    suspend fun loadGame(): SudokuGameState?
    suspend fun deleteGame()

    //Database
    suspend fun storeStatistic()
    suspend fun clearStatistic()
    suspend fun readStatistic(): SudokuGameStats
}
