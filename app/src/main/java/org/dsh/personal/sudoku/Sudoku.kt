package org.dsh.personal.sudoku

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.dsh.personal.sudoku.data.di.sudokuDataDi
import org.dsh.personal.sudoku.domain.sudokuDomainDi
import org.dsh.personal.sudoku.presentation.sudokuPresentationDi
import org.koin.dsl.module


val sudokuDi = module {
    includes(
        sudokuPresentationDi,
        sudokuDomainDi,
        sudokuDataDi,
    )
}

object SudokuRoutes {
    const val PARAM_CONTINUE = "continue"
    @Serializable
    data object MainMenu: NavKey
    @Serializable data class GameScreen(val difficulty: String): NavKey
    @Serializable data object Settings: NavKey
    @Serializable data object Success: NavKey
    @Serializable data object Statistic: NavKey
    @Serializable data object About: NavKey
}
