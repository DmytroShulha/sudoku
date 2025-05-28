package org.dsh.personal.sudoku.domain.useCase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dsh.personal.sudoku.domain.entity.SudokuCellState

class ValidateNoteBoardUseCase() {

    suspend operator fun invoke(
        grid: List<List<SudokuCellState>>,
        cellNumber: Int,
        cellRow: Int,
        cellCol: Int
    ) = withContext(Dispatchers.Default) {
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
            grid[cellRow][cellCol].notes = grid[cellRow][cellCol].notes.toMutableSet().map { if(it.value == cellNumber) cell else it }.toSet()
        }
    }

    fun isSameRow(row1: Int, row2: Int): Boolean = row1 == row2
    fun isSameCol(col1: Int, col2: Int): Boolean = col1 == col2
    fun isSameBlock(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        val blockSize = 3 // Assuming a standard 9x9 Sudoku with 3x3 blocks
        return (row1 / blockSize == row2 / blockSize) && (col1 / blockSize == col2 / blockSize)
    }
}