package org.dsh.personal.sudoku.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class SudokuChange(
    val rowIndex: Int,
    val colIndex: Int,
    val oldValue: Int,
    val newValue: Int,
    val oldIsError: Boolean,
    val newIsError: Boolean,
    val oldIsHighlighted: Boolean,
    val newIsHighlighted: Boolean
    // Add other cell properties if needed for undo/redo
)