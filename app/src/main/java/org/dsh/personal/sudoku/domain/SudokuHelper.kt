package org.dsh.personal.sudoku.domain

import org.dsh.personal.sudoku.domain.entity.SudokuCellState

const val BLOCK_SIZE = 3
const val ROW_SIZE = 9

fun isSameRow(row1: Int, row2: Int): Boolean = row1 == row2
fun isSameCol(col1: Int, col2: Int): Boolean = col1 == col2
fun isSameBlock(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
    return (row1 / BLOCK_SIZE == row2 / BLOCK_SIZE) && (col1 / BLOCK_SIZE == col2 / BLOCK_SIZE)
}

fun markDuplicatesInRow(
    seen: MutableSet<Int>,
    cell: SudokuCellState,
    colIndex: Int,
    row: List<SudokuCellState>
) {
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
}

fun markDuplicateErrorsInSubgrid(
    seen: MutableSet<Int>,
    param: MarkDuplicatesInBlockParam,
) {
    if (param.cell.value != 0) { // Only check non-empty cells
        if (seen.contains(param.cell.value)) {
            // Found a duplicate in this subgrid
            // Mark the current cell as error
            param.cell.isError = true
            // Also find the previous occurrence(s) in this subgrid and mark them
            markDuplicatesInBlock(
                param
            )
        }
        seen.add(param.cell.value)
    }
}

data class MarkDuplicatesInBlockParam(
    val startRow: Int,
    val startCol: Int,
    val rowIndex: Int,
    val colIndex: Int,
    val grid: List<List<SudokuCellState>>,
    val cell: SudokuCellState,
)

private fun markDuplicatesInBlock(
    param: MarkDuplicatesInBlockParam,
) {
    for (prevRowOffset in 0 until BLOCK_SIZE) {
        for (prevColOffset in 0 until BLOCK_SIZE) {
            val prevRowIndex = param.startRow + prevRowOffset
            val prevColIndex = param.startCol + prevColOffset
            if ((prevRowIndex != param.rowIndex || prevColIndex != param.colIndex) &&
                param.grid[prevRowIndex][prevColIndex].value == param.cell.value
            ) {
                param.grid[prevRowIndex][prevColIndex].isError = true
            }
        }
    }
}

fun markDuplicatesInColumn(
    seen: MutableSet<Int>,
    cell: SudokuCellState,
    rowIndex: Int,
    grid: List<List<SudokuCellState>>,
    colIndex: Int
) {
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
}
