package org.dsh.personal.sudoku.domain.entity

data class SudokuBoardTheme(
    val useSystem: Boolean = true,
    val isDark: Boolean = false,
    val isDynamic: Boolean = true,
)

data class SudokuEffects(
    val useHaptic: Boolean = true,
    val useSounds: Boolean = true,
    val soundVolume: Float = 6f,
)