package org.dsh.personal.sudoku.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class SudokuGameStatistic(
    val difficulty: Difficulty = Difficulty.EASY,
    val startTime: Long,
    val endTime: Long,
)
