package org.dsh.personal.sudoku.presentation.success

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.useCase.CurrentGameHandler
import org.dsh.personal.sudoku.domain.useCase.StoreStatisticUseCase
import org.dsh.personal.sudoku.utility.initializeEmptyGame

class SuccessViewModel (
    private val currentGameHandler: CurrentGameHandler,
    private val storeStatistic: StoreStatisticUseCase,
): ViewModel() {
    private val _gameState = MutableStateFlow(initializeEmptyGame())
    val gameState: StateFlow<SudokuGameState> = _gameState.asStateFlow()

    init {
        viewModelScope.launch {
            currentGameHandler.loadGame()?.let { game->
                _gameState.update { game }
                storeStatistic()
            }
        }
    }
}