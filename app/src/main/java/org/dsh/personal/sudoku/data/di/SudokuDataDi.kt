package org.dsh.personal.sudoku.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.dsh.personal.sudoku.core.di.DispatchersQualifiers
import org.dsh.personal.sudoku.data.CurrentGameStorage
import org.dsh.personal.sudoku.data.SudokuGameRepository
import org.dsh.personal.sudoku.data.SudokuGeneratorEasy
import org.dsh.personal.sudoku.data.database.EntryDao
import org.dsh.personal.sudoku.data.database.SudokuDatabase
import org.dsh.personal.sudoku.domain.repository.SudokuGenerator
import org.dsh.personal.sudoku.domain.repository.SudokuRepository
import org.koin.dsl.module

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sudoku_current_game")

val sudokuDataDi = module {
    //Repository
    factory<SudokuRepository> {
        SudokuGameRepository(
            get(),
            get(),
            get(qualifier = DispatchersQualifiers.IO),
            get(qualifier = DispatchersQualifiers.DEFAULT)
        )
    }

    //Sudoku generator
    factory<SudokuGenerator> { SudokuGeneratorEasy() }

    //Storage
    single(qualifier = DataStoreQualifiers.CurrentGameDataStore) { get<Context>().dataStore }
    single { CurrentGameStorage(get(qualifier = DataStoreQualifiers.CurrentGameDataStore)) }

    //Database
    single<SudokuDatabase> { SudokuDatabase.getDatabase(get()) }
    single<EntryDao> { get<SudokuDatabase>().entryDao() }
}
