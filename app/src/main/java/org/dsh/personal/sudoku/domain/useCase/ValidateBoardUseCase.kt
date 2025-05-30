package org.dsh.personal.sudoku.domain.useCase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.dsh.personal.sudoku.domain.entity.SudokuCellNote
import org.dsh.personal.sudoku.domain.entity.SudokuCellState
import org.dsh.personal.sudoku.domain.isSameCol
import org.dsh.personal.sudoku.domain.isSameBlock
import org.dsh.personal.sudoku.domain.isSameRow
import org.dsh.personal.sudoku.domain.ROW_SIZE
import org.dsh.personal.sudoku.domain.BLOCK_SIZE
import org.dsh.personal.sudoku.domain.MarkDuplicatesInBlockParam
import org.dsh.personal.sudoku.domain.markDuplicateErrorsInSubgrid
import org.dsh.personal.sudoku.domain.markDuplicatesInColumn
import org.dsh.personal.sudoku.domain.markDuplicatesInRow

class ValidateBoardUseCase (private val defaultDispatcher: CoroutineDispatcher) {

    private fun Set<SudokuCellNote>.flipVisibility(cellNumber: Int, changeTo: Boolean) =
        map { if (it.value == cellNumber) it.copy(isVisible = changeTo) else it }

    suspend operator fun invoke(
        grid: List<List<SudokuCellState>>,
        cellNumber: Int,
        cellRow: Int,
        cellCol: Int
    ) = withContext(defaultDispatcher) {
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
        validateRows(grid)

        // Validate Columns
        validateColumns(grid)

        // Validate 3x3 Sub grids
        validate3x3SubGrids(grid)
    }

    private fun validate3x3SubGrids(grid: List<List<SudokuCellState>>) {
        for (startRow in 0 until ROW_SIZE step BLOCK_SIZE) {
            for (startCol in 0 until ROW_SIZE step BLOCK_SIZE) {
                val seen = mutableSetOf<Int>()
                // Iterate through the 3x3 subgrid
                for (rowOffset in 0 until BLOCK_SIZE) {
                    for (colOffset in 0 until BLOCK_SIZE) {
                        val rowIndex = startRow + rowOffset
                        val colIndex = startCol + colOffset
                        val cell = grid[rowIndex][colIndex]

                        markDuplicateErrorsInSubgrid(
                            seen = seen,
                            param = MarkDuplicatesInBlockParam(
                                startRow = startRow,
                                startCol = startCol,
                                rowIndex = rowIndex,
                                colIndex = colIndex,
                                grid = grid,
                                cell = cell
                            )
                        )
                    }
                }
            }
        }
    }

    private fun validateColumns(grid: List<List<SudokuCellState>>) {
        for (colIndex in grid[0].indices) {
            val seen = mutableSetOf<Int>()
            for (rowIndex in grid.indices) {
                val cell = grid[rowIndex][colIndex]
                if (cell.value != 0) { // Only check non-empty cells
                    markDuplicatesInColumn(seen, cell, rowIndex, grid, colIndex)
                    seen.add(cell.value)
                }
            }
        }
    }

    private fun validateRows(grid: List<List<SudokuCellState>>) {
        for (rowIndex in grid.indices) {
            val row = grid[rowIndex]
            val seen = mutableSetOf<Int>()
            for (colIndex in row.indices) {
                val cell = row[colIndex]
                if (cell.value != 0) { // Only check non-empty cells
                    markDuplicatesInRow(seen, cell, colIndex, row)
                    seen.add(cell.value)
                }
            }
        }
    }
}
