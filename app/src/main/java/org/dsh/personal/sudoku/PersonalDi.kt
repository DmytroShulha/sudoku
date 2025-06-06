package org.dsh.personal.sudoku

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.dsh.personal.sudoku.core.di.DispatchersQualifiers
import org.koin.dsl.module

val appModule = module {
    @Suppress("InjectDispatcher")
    single<CoroutineDispatcher>(DispatchersQualifiers.IO) { Dispatchers.IO }
    @Suppress("InjectDispatcher")
    single<CoroutineDispatcher>(DispatchersQualifiers.MAIN) { Dispatchers.Main }
    @Suppress("InjectDispatcher")
    single<CoroutineDispatcher>(DispatchersQualifiers.DEFAULT) { Dispatchers.Default }
}

