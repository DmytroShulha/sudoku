package org.dsh.personal.sudoku.presentation

import org.dsh.personal.sudoku.presentation.about.AboutViewModel
import org.dsh.personal.sudoku.presentation.statistic.StatisticViewModel
import org.dsh.personal.sudoku.presentation.success.SuccessViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sudokuPresentationDi = module {
    viewModel {
        SudokuViewModel(
            themeSettingsManager = get(),
            sudokuHandler = get(),
        )
    }
    viewModel { SuccessViewModel(get(), get()) }
    viewModel { StatisticViewModel(get(), get()) }
    viewModel { AboutViewModel(get()) }
}
