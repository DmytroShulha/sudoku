package org.dsh.personal.sudoku

import org.dsh.personal.sudoku.core.AppRoutes
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

    const val MAIN_MENU = "${AppRoutes.SUDOKU_FEATURE}/main_menu"
    const val GAME_SCREEN = "${AppRoutes.SUDOKU_FEATURE}/game_screen/{difficulty}"
    const val SETTINGS_SCREEN = "${AppRoutes.SUDOKU_FEATURE}/settings_screen"
    const val SUCCESS_SCREEN = "${AppRoutes.SUDOKU_FEATURE}/success_screen"
    const val STATISTIC_SCREEN = "${AppRoutes.SUDOKU_FEATURE}/statistic_screen"
    const val ABOUT_SCREEN = "${AppRoutes.SUDOKU_FEATURE}/about_screen"

    fun gameScreenRoute(difficulty: String) = "${AppRoutes.SUDOKU_FEATURE}/game_screen/$difficulty"
}
