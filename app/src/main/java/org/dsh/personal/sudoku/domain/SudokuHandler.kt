package org.dsh.personal.sudoku.domain

import kotlinx.coroutines.CoroutineDispatcher
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.useCase.CalculateAvailableNumbersUseCase
import org.dsh.personal.sudoku.domain.useCase.CurrentGameHandler
import org.dsh.personal.sudoku.domain.useCase.GenerateGameFieldUseCase
import org.dsh.personal.sudoku.domain.useCase.ValidateBoardUseCase
import org.dsh.personal.sudoku.domain.useCase.ValidateNoteBoardUseCase
import kotlin.time.Duration

class SudokuHandler (
    val generateGameField: GenerateGameFieldUseCase,
    val calculateAvailableNumbers: CalculateAvailableNumbersUseCase,
    val validateBoard: ValidateBoardUseCase,
    val validateNoteBoard: ValidateNoteBoardUseCase,
    val defaultCoroutineDispatcher: CoroutineDispatcher,
    val currentGameHandler: CurrentGameHandler,


    ) {
    suspend fun storeGameState(state: SudokuGameState, duration: Duration) {
        currentGameHandler.saveGame(state.copy(duration = duration))
    }
}
