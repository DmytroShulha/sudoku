package org.dsh.personal.sudoku.domain.useCase

import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.repository.SudokuGenerator
import org.dsh.personal.sudoku.domain.repository.SudokuRepository

class GenerateGameFieldUseCase(
    private val repository: SudokuRepository,
    private val sudokuGenerator: SudokuGenerator,
) {
    suspend operator fun invoke(difficulty: Difficulty): Pair<Array<IntArray>, Array<IntArray>> {
        repository.storeStatistic()
        return repository.generateGrid(sudokuGenerator, difficulty)
    }
}