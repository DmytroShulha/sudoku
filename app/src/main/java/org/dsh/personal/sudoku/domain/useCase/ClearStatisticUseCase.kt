package org.dsh.personal.sudoku.domain.useCase

import org.dsh.personal.sudoku.domain.repository.SudokuRepository

class ClearStatisticUseCase (private val repository: SudokuRepository) {
    suspend operator fun invoke() = repository.clearStatistic()
}