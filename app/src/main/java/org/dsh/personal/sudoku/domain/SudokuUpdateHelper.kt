package org.dsh.personal.sudoku.domain

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.entity.InputMode
import org.dsh.personal.sudoku.domain.entity.SudokuBoardState
import org.dsh.personal.sudoku.domain.entity.SudokuCellNote
import org.dsh.personal.sudoku.domain.entity.SudokuCellState
import org.dsh.personal.sudoku.domain.entity.SudokuChange
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.entity.SudokuGameStatistic
import org.dsh.personal.sudoku.domain.entity.SudokuNumberButtonState
import org.dsh.personal.sudoku.utility.toBoard


suspend fun processNote(data: ProcessNoteData, defaultCoroutineDispatcher: CoroutineDispatcher) =
    withContext(defaultCoroutineDispatcher) {
        when (data.currentState.inputMode) {
            InputMode.VALUE -> setValueToCell(data)

            InputMode.NOTES -> updateNotes(data)
        }
    }

data class ProcessNoteData(
    val currentState: SudokuGameState,
    val row: Int,
    val col: Int,
    val number: Int,
    val cellToModify: SudokuCellState,
    val newGrid: SnapshotStateList<SnapshotStateList<SudokuCellState>>,
    val validateBoard: suspend (
        grid: List<List<SudokuCellState>>, cellNumber: Int, cellRow: Int, cellCol: Int
    ) -> Unit,
    val calculateAvailableNumbers: suspend (grid: List<List<Int>>) -> List<SudokuNumberButtonState>,
    val validateNoteBoard: suspend (
        grid: List<List<SudokuCellState>>, cellNumber: Int, cellRow: Int, cellCol: Int
    ) -> Unit
)

suspend fun updateNotes(
    data: ProcessNoteData
): SudokuGameState {
    val updatedCell = if (data.number != 0) {
        data.cellToModify.copy(
            notes = data.cellToModify.notes.toMutableSet().apply {
                    val exists = firstOrNull { it.value == data.number }
                    if (exists != null) remove(exists)
                    else add(SudokuCellNote(value = data.number, isVisible = true, isHighlighted = true))
                }.toSet()
        )
    } else {
        data.cellToModify.copy(notes = emptySet())
    }
    data.newGrid[data.row][data.col] = updatedCell

    // Recalculate highlighting based on the new grid state
    val gridAfterHighlighting = data.newGrid.map { r ->
        r.map { c -> c.copy(isHighlighted = false) }.toMutableStateList()
    }.toMutableStateList() // Clear existing

    highlightSpecifiedNumber(data.number, gridAfterHighlighting)

    data.validateNoteBoard(gridAfterHighlighting, data.number, data.row, data.col)

    val newBoardState = data.currentState.boardState.copy(grid = gridAfterHighlighting)

    return data.currentState.copy(boardState = newBoardState)
}


suspend fun setValueToCell(
    data: ProcessNoteData
): SudokuGameState {
    val change = SudokuChange(
        rowIndex = data.row,
        colIndex = data.col,
        oldValue = data.cellToModify.value,
        newValue = data.number, // The new value
        oldIsError = data.cellToModify.isError,
        newIsError = false, // Will be updated by validateBoard
        oldIsHighlighted = data.cellToModify.isHighlighted,
        newIsHighlighted = false // Will be updated by highlighting logic
    )

    val prevVal = data.newGrid[data.row][data.col].value
    // Update the cell value
    val updatedCell = data.newGrid[data.row][data.col].copy(value = data.number)
    data.newGrid[data.row][data.col] = updatedCell

    // Recalculate highlighting based on the new grid state
    val gridAfterHighlighting = data.newGrid.map { r ->
        r.map { c -> c.copy(isHighlighted = false) }.toMutableStateList()
    }.toMutableStateList() // Clear existing
    highlightSpecifiedNumber(data.number, gridAfterHighlighting)

    // Validate the board after the change
    data.validateBoard(
        gridAfterHighlighting, if (data.number != 0) data.number else prevVal, data.row, data.col
    ) // This will update isError

    val newBoardState = data.currentState.boardState.copy(grid = gridAfterHighlighting)
    val isNowSolved = checkWinCondition(gridAfterHighlighting)

    // Update history, clear redo stack
    val newHistory = data.currentState.history + change
    val newRedoStack = emptyList<SudokuChange>()

    // Update available numbers if you are tracking them based on placed numbers
    val newAvailableNumbers =
        data.calculateAvailableNumbers(data.newGrid.map { newRow -> newRow.map { value -> value.value } })

    return data.currentState.copy(
        boardState = newBoardState,
        isSolved = isNowSolved,
        history = newHistory,
        redoStack = newRedoStack,
        availableNumbers = newAvailableNumbers,
        // Update game statistic if game is solved
        gameStatistic = if (isNowSolved) {
            data.currentState.gameStatistic.copy(endTime = System.currentTimeMillis())
        } else {
            data.currentState.gameStatistic
        }
    )
}


private fun highlightSpecifiedNumber(
    number: Int, gridAfterHighlighting: SnapshotStateList<SnapshotStateList<SudokuCellState>>
) {
    if (number != 0) {
        for (r in gridAfterHighlighting.indices) {
            for (c in gridAfterHighlighting[r].indices) {
                highlightNumberInCell(gridAfterHighlighting, r, c, number)
            }
        }
    } else {
        for (r in gridAfterHighlighting.indices) {
            for (c in gridAfterHighlighting[r].indices) {
                gridAfterHighlighting[r][c].isHighlighted = false
                gridAfterHighlighting[r][c].notes =
                    gridAfterHighlighting[r][c].notes.map { it.copy(isHighlighted = false) }.toSet()
            }
        }
    }
}

private fun highlightNumberInCell(
    gridAfterHighlighting: SnapshotStateList<SnapshotStateList<SudokuCellState>>,
    r: Int,
    c: Int,
    number: Int
) {
    if (gridAfterHighlighting[r][c].value == number) {
        gridAfterHighlighting[r][c].isHighlighted = true
    } else if (gridAfterHighlighting[r][c].value == 0) {
        gridAfterHighlighting[r][c].notes =
            gridAfterHighlighting[r][c].notes.map { it.copy(isHighlighted = it.value == number) }
                .toSet()
    }
}

fun checkWinCondition(grid: List<List<SudokuCellState>>): Boolean {
    // Check if all cells are filled and no cells have `isError = true`
    // And all Sudoku rules are satisfied.
    for (row in grid) {
        for (cell in row) {
            if (cell.isEmpty() || cell.isError) return false
        }
    }
    // Add comprehensive Sudoku rule check here too
    return true // Placeholder
}

suspend fun initializeNewGame(
    difficulty: Difficulty,
    generateGameField: suspend (Difficulty) -> Pair<Array<IntArray>, Array<IntArray>>,
    calculateAvailableNumbers: suspend (List<List<Int>>) -> List<SudokuNumberButtonState>
): SudokuGameState {
    val generateGame = generateGameField(difficulty)
    val puzzleGridValues: List<List<Int>> = generateGame.second.toBoard()

    val initialGrid = puzzleGridValues.mapIndexed { rowIndex, row ->
        row.mapIndexed { colIndex, value ->

            SudokuCellState(
                id = "r${rowIndex}_c${colIndex}",
                value = value,
                isClue = value != 0,
            )
        }.toMutableStateList()
    }.toMutableStateList()


    val board = SudokuBoardState(
        grid = initialGrid,
        potentialSolution = generateGame.first.toBoard(),
        originalPuzzle = puzzleGridValues
    )

    return SudokuGameState(
        boardState = board,
        difficulty = difficulty,
        gameId = System.currentTimeMillis().toString(),
        availableNumbers = calculateAvailableNumbers(puzzleGridValues),
        gameStatistic = SudokuGameStatistic(
            difficulty = difficulty,
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis(),
        )
    )
}
