package org.dsh.personal.sudoku.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class SudokuBoardState(
    val grid: List<List<SudokuCellState>>, // The 9x9 grid
    val originalPuzzle: List<List<Int>>, // The initial state of the puzzle (values only)
    val potentialSolution: List<List<Int>>
) {
    companion object {
        const val SIZE = 9
    }

    // Helper to get a cell at a specific row/col
    fun getCell(row: Int, col: Int): SudokuCellState? {
        return grid.getOrNull(row)?.getOrNull(col)
    }
}
