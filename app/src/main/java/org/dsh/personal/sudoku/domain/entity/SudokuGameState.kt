package org.dsh.personal.sudoku.domain.entity

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
enum class Difficulty {
    EASY, MEDIUM, HARD, EXPERT
}

@Serializable
enum class InputMode {
    VALUE, NOTES
}

@Serializable
data class SudokuGameState(
    val boardState: SudokuBoardState,
    val selectedCell: Pair<Int, Int>? = null, // (row, col) of the currently selected cell
    val selectedNumberForInput: Int? = null, // Number selected from the number pad (1-9)
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val isSolved: Boolean = false,
    val gameStatistic: SudokuGameStatistic,
    val mistakesMade: Int = 0, // Optional: for tracking mistakes
    val timerMillis: Long = 0L, // Optional: for game timer
    val hintsRemaining: Int = 3, // Optional: for hint system
    val gameId: String, // Unique ID for this specific game instance/puzzle
    val availableNumbers: List<SudokuNumberButtonState> = (1..9).map { number -> SudokuNumberButtonState(number, true, 9) },
    val history: List<SudokuChange> = emptyList(),
    val redoStack: List<SudokuChange> = emptyList(),
    val inputMode: InputMode = InputMode.VALUE, // Add input mode state
    val duration: Duration = Duration.ZERO,

    )