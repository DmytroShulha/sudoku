package org.dsh.personal.sudoku.domain.useCase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.dsh.personal.sudoku.domain.entity.SudokuCellState
import org.dsh.personal.sudoku.domain.isSameCol
import org.dsh.personal.sudoku.domain.isSameBlock
import org.dsh.personal.sudoku.domain.isSameRow

class ValidateNoteBoardUseCase(private val defaultDispatcher: CoroutineDispatcher) {

    suspend operator fun invoke(
        grid: List<List<SudokuCellState>>,
        cellNumber: Int,
        cellRow: Int,
        cellCol: Int
    ) = withContext(defaultDispatcher) {
        if(cellNumber != 0 && grid[cellRow][cellCol].notes.any{ it.value == cellNumber }) {
            var cell = grid[cellRow][cellCol].notes.first { it.value == cellNumber }
            for (r in grid.indices) {
                for (c in grid[r].indices) {
                    if (isSameBlock(cellRow, cellCol, r, c) && grid[r][c].value == cellNumber) {
                        cell = cell.copy(isError = true)
                    }
                    if (isSameRow(cellRow, r) && grid[r][c].value == cellNumber) {
                        cell = cell.copy(isError = true)
                    }
                    if (isSameCol(cellCol, c) && grid[r][c].value == cellNumber) {
                        cell = cell.copy(isError = true)
                    }
                }
            }
            grid[cellRow][cellCol].notes = grid[cellRow][cellCol].notes.toMutableSet()
                .map { if (it.value == cellNumber) cell else it }.toSet()
        }
    }
}
