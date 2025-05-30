package org.dsh.personal.sudoku.domain.repository

import kotlinx.coroutines.CoroutineDispatcher
import org.dsh.personal.sudoku.domain.entity.Difficulty

interface SudokuGenerator {
    suspend fun generate(
        difficulty: Difficulty,
        defaultDispatcher: CoroutineDispatcher,
    ): Pair<Array<IntArray>, Array<IntArray>>
}
