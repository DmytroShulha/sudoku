package org.dsh.personal.sudoku.utility

fun Array<IntArray>.toBoard(): List<List<Int>> = map { it.toList() }
