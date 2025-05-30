package org.dsh.personal.sudoku.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class SudokuCellState(
    val id: String, // Unique identifier, e.g., "row0_col0"
    var value: Int, // 0 for empty, 1-9 for numbers
    val isClue: Boolean, // True if this cell was part of the initial puzzle
    var isError: Boolean = false, // True if this cell currently violates a Sudoku rule
    var notes: Set<SudokuCellNote> = emptySet(), // For user's pencil marks (optional)
    var isHighlighted: Boolean = false // For highlighting the cell (optional)
) {
    // Helper to check if cell is empty
    fun isEmpty(): Boolean = value == 0
}

@Serializable
data class SudokuCellNote(
    val value: Int,
    val isHighlighted: Boolean = false,
    val isError: Boolean = false,
    val isVisible: Boolean = true,
)
