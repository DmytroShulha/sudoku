package org.dsh.personal.sudoku.domain

import org.dsh.personal.sudoku.core.di.DispatchersQualifiers
import org.dsh.personal.sudoku.domain.useCase.CalculateAvailableNumbersUseCase
import org.dsh.personal.sudoku.domain.useCase.ClearStatisticUseCase
import org.dsh.personal.sudoku.domain.useCase.CurrentGameHandler
import org.dsh.personal.sudoku.domain.useCase.GenerateGameFieldUseCase
import org.dsh.personal.sudoku.domain.useCase.ReadStatisticUseCase
import org.dsh.personal.sudoku.domain.useCase.StoreStatisticUseCase
import org.dsh.personal.sudoku.domain.useCase.ValidateBoardUseCase
import org.dsh.personal.sudoku.domain.useCase.ValidateNoteBoardUseCase
import org.dsh.personal.sudoku.presentation.game.ThemeSettingsManager
import org.koin.dsl.module

val sudokuDomainDi = module {
    single { ThemeSettingsManager(get()) }
    factory { GenerateGameFieldUseCase(get(), get()) }
    factory { CalculateAvailableNumbersUseCase(get(DispatchersQualifiers.DEFAULT)) }
    factory { ValidateBoardUseCase(get(DispatchersQualifiers.DEFAULT)) }
    factory { ValidateNoteBoardUseCase(get(DispatchersQualifiers.DEFAULT)) }
    factory { CurrentGameHandler(get()) }
    factory { StoreStatisticUseCase(get()) }
    factory { ReadStatisticUseCase(get()) }
    factory { ClearStatisticUseCase(get()) }
    factory { SudokuHandler(get(), get(), get(), get(), get(qualifier = DispatchersQualifiers.DEFAULT), get()) }
}
