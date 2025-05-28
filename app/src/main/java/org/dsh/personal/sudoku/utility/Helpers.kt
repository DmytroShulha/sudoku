package org.dsh.personal.sudoku.utility

import androidx.compose.runtime.toMutableStateList
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.entity.SudokuBoardState
import org.dsh.personal.sudoku.domain.entity.SudokuCellState
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.entity.SudokuGameStatistic

fun initializeEmptyGame(): SudokuGameState {

    val puzzleGridValues: List<List<Int>> = generateEmptyPuzzle()

    val initialGrid = puzzleGridValues.mapIndexed { rowIndex, row ->
        row.mapIndexed { colIndex, value ->
            SudokuCellState(
                id = "r${rowIndex}_c${colIndex}",
                value = value,
                isClue = value != 0 // If value is not 0, it's a clue
            )
        }
            .toMutableStateList() // Using toMutableStateList for individual cell observability if needed
    }.toMutableStateList()


    val board = SudokuBoardState(
        grid = initialGrid,
        originalPuzzle = puzzleGridValues,
        potentialSolution = emptyList()
    )

    return SudokuGameState(
        boardState = board,
        difficulty = Difficulty.EASY,
        gameId = System.currentTimeMillis().toString(), // Simple unique ID
        gameStatistic = SudokuGameStatistic(
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis()
        )
    )
}

private fun generateEmptyPuzzle(): List<List<Int>> {
    return List(SudokuBoardState.SIZE) { List(SudokuBoardState.SIZE) { 0 } } // Default empty
}