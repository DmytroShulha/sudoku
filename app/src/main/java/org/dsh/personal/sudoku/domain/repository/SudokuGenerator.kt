package org.dsh.personal.sudoku.domain.repository

import org.dsh.personal.sudoku.domain.entity.Difficulty

interface SudokuGenerator {
    suspend fun generate(difficulty: Difficulty): Pair<Array<IntArray>, Array<IntArray>>
}