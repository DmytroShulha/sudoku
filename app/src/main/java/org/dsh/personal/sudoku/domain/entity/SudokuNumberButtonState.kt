package org.dsh.personal.sudoku.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class SudokuNumberButtonState(
    val number: Int,
    val isPossible: Boolean,
    val availableCount: Int,
)
