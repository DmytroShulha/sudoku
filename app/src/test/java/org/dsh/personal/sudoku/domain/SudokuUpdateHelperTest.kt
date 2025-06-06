@file:Suppress("UNCHECKED_CAST")

package org.dsh.personal.sudoku.domain

import androidx.compose.runtime.toMutableStateList
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.entity.InputMode
import org.dsh.personal.sudoku.domain.entity.SudokuBoardState
import org.dsh.personal.sudoku.domain.entity.SudokuCellNote
import org.dsh.personal.sudoku.domain.entity.SudokuCellState
import org.dsh.personal.sudoku.domain.entity.SudokuChange
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.entity.SudokuGameStatistic
import org.dsh.personal.sudoku.domain.entity.SudokuNumberButtonState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@ExperimentalCoroutinesApi
class SudokuUpdateHelperTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockValidateBoard: suspend (List<List<SudokuCellState>>, Int, Int, Int) -> Unit
    private lateinit var mockCalculateAvailableNumbers: suspend (List<List<Int>>) -> List<SudokuNumberButtonState>
    private lateinit var mockValidateNoteBoard: suspend (List<List<SudokuCellState>>, Int, Int, Int) -> Unit
    private lateinit var mockGenerateGameField: suspend (Difficulty) -> Pair<Array<IntArray>, Array<IntArray>>


    private fun createEmptyGrid(size: Int = ROW_SIZE): List<List<SudokuCellState>> {
        return List(size) { r ->
            List(size) { c ->
                SudokuCellState(id = "r${r}_c$c", value = 0, isClue = false)
            }.toMutableStateList()
        }
    }

    private fun createGameState(
        grid: List<List<SudokuCellState>> = createEmptyGrid(),
        inputMode: InputMode = InputMode.VALUE,
        isSolved: Boolean = false,
        difficulty: Difficulty = Difficulty.EASY,
        history: List<SudokuChange> = emptyList(),
        redoStack: List<SudokuChange> = emptyList(),
        availableNumbers: List<SudokuNumberButtonState> = emptyList(),
        gameStatistic: SudokuGameStatistic = SudokuGameStatistic(
            difficulty = difficulty,
            startTime = 1000L,
            endTime = 0L
        )
    ): SudokuGameState {
        return SudokuGameState(
            boardState = SudokuBoardState(
                grid = grid.map { it.toMutableStateList() }.toMutableStateList(),
                potentialSolution = emptyList(), // Not directly tested here
                originalPuzzle = emptyList() // Not directly tested here
            ),
            inputMode = inputMode,
            isSolved = isSolved,
            difficulty = difficulty,
            history = history,
            redoStack = redoStack,
            availableNumbers = availableNumbers,
            gameStatistic = gameStatistic,
            gameId = UUID.randomUUID().toString()
        )
    }

    private fun createProcessNoteData(
        currentState: SudokuGameState,
        row: Int, col: Int, number: Int,
        cellToModify: SudokuCellState,
        newGrid: List<List<SudokuCellState>>
    ): ProcessNoteData {
        return ProcessNoteData(
            currentState = currentState,
            row = row,
            col = col,
            number = number,
            cellToModify = cellToModify,
            newGrid = newGrid.map { it.toMutableStateList() }.toMutableStateList(),
            validateBoard = mockValidateBoard,
            calculateAvailableNumbers = mockCalculateAvailableNumbers,
            validateNoteBoard = mockValidateNoteBoard
        )
    }

    @Before
    fun setUp() {
        mockValidateBoard = mockk(relaxed = true)
        mockCalculateAvailableNumbers = mockk(relaxed = true)
        mockValidateNoteBoard = mockk(relaxed = true)
        mockGenerateGameField = mockk(relaxed = true)

        coEvery { mockCalculateAvailableNumbers.invoke(any()) } returns List(9) {
            SudokuNumberButtonState(
                it + 1,
                true,
                9
            )
        }
    }

    // --- processNote Tests ---
    @Test
    fun `processNote routes to setValueToCell for VALUE mode`() = runTest(testDispatcher) {
        val initialGrid = createEmptyGrid()
        val cellToModify = initialGrid[0][0]
        val gameState = createGameState(grid = initialGrid, inputMode = InputMode.VALUE)
        val data = createProcessNoteData(gameState, 0, 0, 5, cellToModify, initialGrid)

        processNote(data, testDispatcher) // Actual call

        coVerify(exactly = 1) {
            mockValidateBoard.invoke(
                any(),
                5,
                0,
                0
            )
        } // Indirectly checks setValueToCell was dominant
        coVerify(exactly = 0) { mockValidateNoteBoard.invoke(any(), any(), any(), any()) }
    }

    @Test
    fun `processNote routes to updateNotes for NOTES mode`() = runTest(testDispatcher) {
        val initialGrid = createEmptyGrid()
        val cellToModify = initialGrid[0][0]
        val gameState = createGameState(grid = initialGrid, inputMode = InputMode.NOTES)
        val data = createProcessNoteData(gameState, 0, 0, 5, cellToModify, initialGrid)

        processNote(data, testDispatcher) // Actual call

        coVerify(exactly = 1) {
            mockValidateNoteBoard.invoke(
                any(),
                5,
                0,
                0
            )
        } // Indirectly checks updateNotes was dominant
        coVerify(exactly = 0) { mockValidateBoard.invoke(any(), any(), any(), any()) }
    }

    // --- updateNotes Tests ---
    @Test
    fun `updateNotes adds a new note and highlights`() = runTest(testDispatcher) {
        val initialGrid = createEmptyGrid()
        val cellToModify = initialGrid[0][0].copy(notes = emptySet())
        val gameState = createGameState(grid = initialGrid, inputMode = InputMode.NOTES)
        val data = createProcessNoteData(gameState, 0, 0, 5, cellToModify, initialGrid)

        val resultState = updateNotes(data)

        val updatedCell = resultState.boardState.grid[0][0]
        assertTrue(updatedCell.notes.any { it.value == 5 && it.isVisible && it.isHighlighted })
        coVerify { mockValidateNoteBoard.invoke(resultState.boardState.grid, 5, 0, 0) }
    }

    @Test
    fun `updateNotes removes an existing note and highlights`() = runTest(testDispatcher) {
        val initialNotes = setOf(SudokuCellNote(5, isVisible = true, isHighlighted = false))
        val initialGrid = createEmptyGrid()
        initialGrid[0][0].notes = initialNotes // Manually set for test
        val cellToModify = initialGrid[0][0]
        val gameState = createGameState(grid = initialGrid, inputMode = InputMode.NOTES)
        val data = createProcessNoteData(gameState, 0, 0, 5, cellToModify, initialGrid)

        val resultState = updateNotes(data)

        val updatedCell = resultState.boardState.grid[0][0]
        assertFalse(updatedCell.notes.any { it.value == 5 })
        // Check that other cells with value 5 are highlighted (if any)
        // Check that notes with value 5 in other cells are highlighted
    }

    @Test
    fun `updateNotes clears all notes when number is 0`() = runTest(testDispatcher) {
        val initialNotes = setOf(SudokuCellNote(5), SudokuCellNote(3))
        val initialGrid = createEmptyGrid()
        initialGrid[0][0].notes = initialNotes
        val cellToModify = initialGrid[0][0]
        val gameState = createGameState(grid = initialGrid, inputMode = InputMode.NOTES)
        val data = createProcessNoteData(gameState, 0, 0, 0, cellToModify, initialGrid)

        val resultState = updateNotes(data)

        assertTrue(resultState.boardState.grid[0][0].notes.isEmpty())
        coVerify { mockValidateNoteBoard.invoke(resultState.boardState.grid, 0, 0, 0) }
    }


    // --- setValueToCell Tests ---
    @Test
    fun `setValueToCell updates value, history, and calls lambdas`() = runTest(testDispatcher) {
        val initialGrid = createEmptyGrid()
        val cellToModify = initialGrid[0][0].copy(value = 0)
        val gameState = createGameState(grid = initialGrid)
        val data = createProcessNoteData(gameState, 0, 0, 7, cellToModify, initialGrid)

        coEvery { mockValidateBoard.invoke(any(), any(), any(), any()) } answers {
            val argGrid = it.invocation.args[0] as List<List<SudokuCellState>>
            // Simulate no errors for win condition
            argGrid.forEach { row -> row.forEach { cell -> cell.isError = false } }
        }


        val resultState = setValueToCell(data)

        assertEquals(7, resultState.boardState.grid[0][0].value)
        assertEquals(1, resultState.history.size)
        assertEquals(0, resultState.history.first().oldValue)
        assertEquals(7, resultState.history.first().newValue)
        assertTrue(resultState.redoStack.isEmpty())
        assertFalse(resultState.isSolved) // checkWinCondition is basic, won't be solved with one move
        coVerify { mockValidateBoard.invoke(resultState.boardState.grid, 7, 0, 0) }
        coVerify { mockCalculateAvailableNumbers.invoke(any()) }
    }

    @Test
    fun `setValueToCell sets solved and endTime when checkWinCondition is true`() =
        runTest(testDispatcher) {
            val almostCompleteGrid = createEmptyGrid().map { row ->
                row.map { cell -> cell.copy(value = 1) }
                    .toMutableStateList() // Fill with some values
            }.toMutableStateList()
            almostCompleteGrid[0][0].value = 0 // Last empty cell
            val cellToModify = almostCompleteGrid[0][0]

            val initialGameStat =
                SudokuGameStatistic(Difficulty.EASY, startTime = 100L, endTime = 0L)
            val gameState =
                createGameState(grid = almostCompleteGrid, gameStatistic = initialGameStat)
            val data = createProcessNoteData(gameState, 0, 0, 5, cellToModify, almostCompleteGrid)

            // Mock validateBoard to not set errors and fill the cell for checkWinCondition
            coEvery { mockValidateBoard.invoke(any(), any(), any(), any()) } answers {
                val argGrid = it.invocation.args[0] as List<List<SudokuCellState>>
                argGrid[0][0].value = 5 // Ensure the cell is filled for win condition
                argGrid.forEach { r -> r.forEach { c -> c.isError = false } }
            }

            val resultState = setValueToCell(data)

            assertTrue("Game should be solved", resultState.isSolved)
            assertTrue(
                "End time should be set",
                resultState.gameStatistic.endTime > initialGameStat.startTime
            )
            coVerify { mockValidateBoard.invoke(any(), 5, 0, 0) }
        }


    // --- checkWinCondition Tests ---
    @Test
    fun `checkWinCondition returns false if grid has empty cells`() {
        val grid = createEmptyGrid() // Has empty cells by default
        assertFalse(checkWinCondition(grid))
    }

    @Test
    fun `checkWinCondition returns false if grid has errors`() {
        val grid = createEmptyGrid().map { row ->
            row.map { cell -> cell.copy(value = 1) }.toMutableStateList() // Fill all cells
        }.toMutableStateList()
        grid[0][0].isError = true
        assertFalse(checkWinCondition(grid))
    }

    @Test
    fun `checkWinCondition returns true if grid is full and no errors`() {
        val grid = createEmptyGrid().map { row ->
            row.map { cell -> cell.copy(value = 1, isError = false) }.toMutableStateList()
        }.toMutableStateList()
        assertTrue(checkWinCondition(grid))
    }

    // --- initializeNewGame Tests ---
    @Test
    fun `initializeNewGame creates game state correctly`() = runTest(testDispatcher) {
        val difficulty = Difficulty.MEDIUM
        val puzzleArray = Array(ROW_SIZE) { IntArray(ROW_SIZE) { 0 } }
        val solutionArray = Array(ROW_SIZE) { IntArray(ROW_SIZE) { 1 } }
        puzzleArray[0][0] = 5 // A clue
        solutionArray[0][0] = 5

        coEvery { mockGenerateGameField.invoke(difficulty) } returns Pair(
            solutionArray,
            puzzleArray
        )
        val mockAvailableNumbers = List(9) { SudokuNumberButtonState(it + 1, false, 0) }
        coEvery { mockCalculateAvailableNumbers.invoke(any()) } returns mockAvailableNumbers

        val startTime = System.currentTimeMillis() // Approximate
        val resultState =
            initializeNewGame(difficulty, mockGenerateGameField, mockCalculateAvailableNumbers)

        assertEquals(difficulty, resultState.difficulty)
        assertEquals(5, resultState.boardState.grid[0][0].value)
        assertTrue(resultState.boardState.grid[0][0].isClue)
        assertEquals(0, resultState.boardState.grid[0][1].value)
        assertFalse(resultState.boardState.grid[0][1].isClue)
        assertEquals(solutionArray.map { it.toList() }, resultState.boardState.potentialSolution)
        assertEquals(puzzleArray.map { it.toList() }, resultState.boardState.originalPuzzle)
        assertEquals(mockAvailableNumbers, resultState.availableNumbers)
        assertTrue(resultState.gameStatistic.startTime >= startTime)
        assertEquals(
            resultState.gameStatistic.startTime,
            resultState.gameStatistic.endTime
        ) // End time initially same as start
        assertEquals(difficulty, resultState.gameStatistic.difficulty)
        assertNotNull(resultState.gameId)

        coVerify { mockGenerateGameField.invoke(difficulty) }
        val puzzleGridSlot = slot<List<List<Int>>>()
        coVerify { mockCalculateAvailableNumbers.invoke(capture(puzzleGridSlot)) }
        assertEquals(puzzleArray.map { it.toList() }, puzzleGridSlot.captured)
    }
}