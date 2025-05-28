package org.dsh.personal.sudoku.domain.useCase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dsh.personal.sudoku.domain.entity.SudokuCellNote
import org.dsh.personal.sudoku.domain.entity.SudokuCellState

class ValidateBoardUseCase() {

    fun Set<SudokuCellNote>.flipVisibility(cellNumber: Int, changeTo: Boolean) =
        map { if (it.value == cellNumber) it.copy(isVisible = changeTo) else it }

    suspend operator fun invoke(
        grid: List<List<SudokuCellState>>,
        cellNumber: Int,
        cellRow: Int,
        cellCol: Int
    ) = withContext(Dispatchers.Default) {
        for (r in grid.indices) {
            for (c in grid[r].indices) {
                grid[r][c].apply {
                    isError = false // Clear cell error
                    notes = notes.map { it.copy(isHighlighted = false) }.toSet()
                }
            }
        }

        //Logic to remove conflicting notes

        val makeVisible = grid[cellRow][cellCol].value == 0
        for (r in grid.indices) {
            for (c in grid[r].indices) {
                if (isSameBlock(cellRow, cellCol, r, c) && grid[r][c].value == 0) {
                    grid[r][c].notes = grid[r][c].notes.toMutableSet()
                        .flipVisibility(cellNumber, makeVisible)
                        .toSet()
                }
                if (isSameRow(cellRow, r) && grid[r][c].value == 0) {
                    grid[r][c].notes = grid[r][c].notes.toMutableSet()
                        .flipVisibility(cellNumber, makeVisible)
                        .toSet()
                }
                if (isSameCol(cellCol, c) && grid[r][c].value == 0) {
                    grid[r][c].notes = grid[r][c].notes.toMutableSet()
                        .flipVisibility(cellNumber, makeVisible)
                        .toSet()
                }
            }
        }

        // Validate Rows
        for (rowIndex in grid.indices) {
            val row = grid[rowIndex]
            val seen = mutableSetOf<Int>()
            for (colIndex in row.indices) {
                val cell = row[colIndex]
                if (cell.value != 0) { // Only check non-empty cells
                    if (seen.contains(cell.value)) {
                        // Found a duplicate in this row
                        // Mark the current cell as error
                        cell.isError = true
                        // Also find the previous occurrence(s) of this number in the row and mark them
                        for (prevColIndex in 0 until colIndex) {
                            if (row[prevColIndex].value == cell.value) {
                                row[prevColIndex].isError = true
                            }
                        }
                    }
                    seen.add(cell.value)
                }
            }
        }

        // Validate Columns
        for (colIndex in grid[0].indices) {
            val seen = mutableSetOf<Int>()
            for (rowIndex in grid.indices) {
                val cell = grid[rowIndex][colIndex]
                if (cell.value != 0) { // Only check non-empty cells
                    if (seen.contains(cell.value)) {
                        // Found a duplicate in this column
                        // Mark the current cell as error
                        cell.isError = true
                        // Also find the previous occurrence(s) of this number in the column and mark them
                        for (prevRowIndex in 0 until rowIndex) {
                            if (grid[prevRowIndex][colIndex].value == cell.value) {
                                grid[prevRowIndex][colIndex].isError = true
                            }
                        }
                    }
                    seen.add(cell.value)
                }
            }
        }

        // Validate 3x3 Sub grids
        for (startRow in 0 until 9 step 3) {
            for (startCol in 0 until 9 step 3) {
                val seen = mutableSetOf<Int>()
                // Iterate through the 3x3 subgrid
                for (rowOffset in 0 until 3) {
                    for (colOffset in 0 until 3) {
                        val rowIndex = startRow + rowOffset
                        val colIndex = startCol + colOffset
                        val cell = grid[rowIndex][colIndex]

                        if (cell.value != 0) { // Only check non-empty cells
                            if (seen.contains(cell.value)) {
                                // Found a duplicate in this subgrid
                                // Mark the current cell as error
                                cell.isError = true
                                // Also find the previous occurrence(s) in this subgrid and mark them
                                for (prevRowOffset in 0 until 3) {
                                    for (prevColOffset in 0 until 3) {
                                        val prevRowIndex = startRow + prevRowOffset
                                        val prevColIndex = startCol + prevColOffset
                                        if ((prevRowIndex != rowIndex || prevColIndex != colIndex) &&
                                            grid[prevRowIndex][prevColIndex].value == cell.value
                                        ) {
                                            grid[prevRowIndex][prevColIndex].isError = true
                                        }
                                    }
                                }
                            }
                            seen.add(cell.value)
                        }
                    }
                }
            }
        }
    }

    fun isSameRow(row1: Int, row2: Int): Boolean = row1 == row2
    fun isSameCol(col1: Int, col2: Int): Boolean = col1 == col2
    fun isSameBlock(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        val blockSize = 3 // Assuming a standard 9x9 Sudoku with 3x3 blocks
        return (row1 / blockSize == row2 / blockSize) && (col1 / blockSize == col2 / blockSize)
    }
}