package org.dsh.personal.sudoku.data.di

import org.koin.core.qualifier.named

object DataStoreQualifiers {
    val CurrentGameDataStore = named("CurrentGameDataStore")
}