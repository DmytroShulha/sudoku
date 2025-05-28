package org.dsh.personal.sudoku.data

import org.dsh.personal.sudoku.data.database.EntryDao
import org.dsh.personal.sudoku.data.database.SudokuDatabase
import org.dsh.personal.sudoku.domain.repository.SudokuGenerator
import org.dsh.personal.sudoku.domain.repository.SudokuRepository
import org.koin.dsl.module

val sudokuDataDi = module {
    //Repository
    factory<SudokuRepository> { SudokuGameRepository(get(), get()) }

    //Sudoku generator
    factory<SudokuGenerator> { SudokuGeneratorEasy() }

    //Storage
    single { CurrentGameStorage(get()) }

    //Database
    single<SudokuDatabase> { SudokuDatabase.getDatabase(get()) }
    single<EntryDao> { get<SudokuDatabase>().entryDao() }
}