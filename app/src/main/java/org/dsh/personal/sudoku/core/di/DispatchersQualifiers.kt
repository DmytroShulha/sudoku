package org.dsh.personal.sudoku.core.di

import org.koin.core.qualifier.named

object DispatchersQualifiers {
    val MAIN = named("MAIN")
    val IO = named("IO")
    val DEFAULT = named("DEFAULT")
}
