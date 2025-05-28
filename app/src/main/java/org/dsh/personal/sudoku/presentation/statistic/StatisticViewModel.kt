package org.dsh.personal.sudoku.presentation.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dsh.personal.sudoku.domain.entity.SudokuGameStats
import org.dsh.personal.sudoku.domain.useCase.ClearStatisticUseCase
import org.dsh.personal.sudoku.domain.useCase.ReadStatisticUseCase

class StatisticViewModel (
    private val readStatistic: ReadStatisticUseCase,
    private val clearStatistic: ClearStatisticUseCase,
): ViewModel() {
    private val _gameStat = MutableStateFlow(GameStat())
    val gameStat: StateFlow<GameStat> = _gameStat.asStateFlow()

    init {
        viewModelScope.launch {
            _gameStat.update { it.copy(gameStats = readStatistic(), isLoading = false) }
        }
    }

    fun clearStat() {
        viewModelScope.launch {
            clearStatistic()
            _gameStat.update { it.copy(gameStats = SudokuGameStats()) }
        }
    }

    data class GameStat(val gameStats: SudokuGameStats? = null, val isLoading: Boolean = true)
}