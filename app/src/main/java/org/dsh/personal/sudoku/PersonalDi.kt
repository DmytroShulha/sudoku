package org.dsh.personal.sudoku

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    includes(coroutineDispatchersModule)
}

val coroutineDispatchersModule = module {
    @Suppress("InjectDispatcher")
    single<CoroutineDispatcher>(named("IO")) { Dispatchers.IO }
    @Suppress("InjectDispatcher")
    single<CoroutineDispatcher>(named("MAIN")) { Dispatchers.Main }
    @Suppress("InjectDispatcher")
    single<CoroutineDispatcher>(named("DEFAULT")) { Dispatchers.Default }
}
