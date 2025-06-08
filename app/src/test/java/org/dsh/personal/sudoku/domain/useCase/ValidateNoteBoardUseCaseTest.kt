package org.dsh.personal.sudoku.domain.useCase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dsh.personal.sudoku.domain.ROW_SIZE
import org.dsh.personal.sudoku.domain.entity.SudokuCellNote
import org.dsh.personal.sudoku.domain.entity.SudokuCellState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ValidateNoteBoardUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: ValidateNoteBoardUseCase

    // Using locally defined ROW_SIZE for clarity in test setup,
    // assuming it matches the domain constants (e.g., 9 for Sudoku).
    private val testRowSize = ROW_SIZE

    @Before
    fun setUp() {
        useCase = ValidateNoteBoardUseCase(testDispatcher)
    }

    private fun createCell(
        row: Int,
        col: Int,
        value: Int = 0,
        notes: Set<SudokuCellNote> = emptySet()
    ): SudokuCellState {
        return SudokuCellState(id = "${row}_${col}", value = value, notes = notes, isClue = true)
    }

    private fun createTestGrid(
        rows: Int = testRowSize,
        cols: Int = testRowSize,
        initialCellSetup: (r: Int, c: Int) -> SudokuCellState = { r, c -> createCell(r, c) }
    ): MutableList<MutableList<SudokuCellState>> {
        return MutableList(rows) { r ->
            MutableList(cols) { c ->
                initialCellSetup(r, c)
            }
        }
    }

    private fun getNoteFromCell(
        grid: List<List<SudokuCellState>>,
        r: Int,
        c: Int,
        number: Int
    ): SudokuCellNote? {
        return grid[r][c].notes.find { it.value == number }
    }

    @Test
    fun `invoke with cellNumber 0 does not modify notes`() = runTest(testDispatcher) {
        val initialNotes = setOf(SudokuCellNote(5, isError = false))
        val grid = createTestGrid { r, c ->
            if (r == 0 && c == 0) createCell(r, c, notes = initialNotes) else createCell(r, c)
        }
        val originalNotes = grid[0][0].notes.toSet() // Keep a copy

        useCase(grid, cellNumber = 0, cellRow = 0, cellCol = 0)

        assertEquals("Notes should not change if cellNumber is 0", originalNotes, grid[0][0].notes)
        assertFalse(getNoteFromCell(grid, 0, 0, 5)!!.isError)
    }

    @Test
    fun `invoke with cellNumber not in notes does not modify notes`() = runTest(testDispatcher) {
        val initialNotes = setOf(SudokuCellNote(3, isError = false))
        val grid = createTestGrid { r, c ->
            if (r == 0 && c == 0) createCell(r, c, notes = initialNotes) else createCell(r, c)
        }
        grid[1][1] = createCell(1, 1, value = 5) // Potential conflict if note 5 existed
        val originalNotes = grid[0][0].notes.toSet()

        useCase(
            grid,
            cellNumber = 5,
            cellRow = 0,
            cellCol = 0
        ) // Note for 5 doesn't exist in cell (0,0)

        assertEquals(
            "Notes should not change if cellNumber is not in target cell's notes",
            originalNotes,
            grid[0][0].notes
        )
        assertFalse(getNoteFromCell(grid, 0, 0, 3)!!.isError)
    }

    @Test
    fun `invoke with no conflicts does not set error on note`() = runTest(testDispatcher) {
        val targetNumber = 5
        val initialNotes =
            setOf(SudokuCellNote(targetNumber, isError = false), SudokuCellNote(2, isError = false))
        val grid = createTestGrid { r, c ->
            if (r == 0 && c == 0) createCell(r, c, notes = initialNotes) else createCell(r, c)
        }
        // No other cell has value 5

        useCase(grid, cellNumber = targetNumber, cellRow = 0, cellCol = 0)

        val targetNote = getNoteFromCell(grid, 0, 0, targetNumber)
        assertNotNull(targetNote)
        assertFalse(
            "Note for $targetNumber should not be an error as no conflicts exist",
            targetNote!!.isError
        )
        assertFalse(
            "Note for 2 should remain not an error",
            getNoteFromCell(grid, 0, 0, 2)!!.isError
        )
    }

    @Test
    fun `invoke with block conflict sets error on note`() = runTest(testDispatcher) {
        val targetNumber = 5
        val initialNotes = setOf(SudokuCellNote(targetNumber, isError = false))
        val grid = createTestGrid { r, c ->
            when {
                r == 0 && c == 0 -> createCell(r, c, notes = initialNotes)
                r == 1 && c == 1 -> createCell(r, c, value = targetNumber) // Conflict in same block
                else -> createCell(r, c)
            }
        }

        useCase(grid, cellNumber = targetNumber, cellRow = 0, cellCol = 0)

        val targetNote = getNoteFromCell(grid, 0, 0, targetNumber)
        assertNotNull(targetNote)
        assertTrue(
            "Note for $targetNumber should be an error due to block conflict",
            targetNote!!.isError
        )
    }

    @Test
    fun `invoke with row conflict sets error on note`() = runTest(testDispatcher) {
        val targetNumber = 5
        val initialNotes = setOf(SudokuCellNote(targetNumber, isError = false))
        val grid = createTestGrid { r, c ->
            when {
                r == 0 && c == 0 -> createCell(r, c, notes = initialNotes)
                r == 0 && c == 5 -> createCell(r, c, value = targetNumber) // Conflict in same row
                else -> createCell(r, c)
            }
        }

        useCase(grid, cellNumber = targetNumber, cellRow = 0, cellCol = 0)

        val targetNote = getNoteFromCell(grid, 0, 0, targetNumber)
        assertNotNull(targetNote)
        assertTrue(
            "Note for $targetNumber should be an error due to row conflict",
            targetNote!!.isError
        )
    }

    @Test
    fun `invoke with column conflict sets error on note`() = runTest(testDispatcher) {
        val targetNumber = 5
        val initialNotes = setOf(SudokuCellNote(targetNumber, isError = false))
        val grid = createTestGrid { r, c ->
            when {
                r == 0 && c == 0 -> createCell(r, c, notes = initialNotes)
                r == 5 && c == 0 -> createCell(
                    r,
                    c,
                    value = targetNumber
                ) // Conflict in same column
                else -> createCell(r, c)
            }
        }

        useCase(grid, cellNumber = targetNumber, cellRow = 0, cellCol = 0)

        val targetNote = getNoteFromCell(grid, 0, 0, targetNumber)
        assertNotNull(targetNote)
        assertTrue(
            "Note for $targetNumber should be an error due to column conflict",
            targetNote!!.isError
        )
    }

    @Test
    fun `invoke with multiple conflicts sets error on note`() = runTest(testDispatcher) {
        val targetNumber = 5
        val initialNotes = setOf(SudokuCellNote(targetNumber, isError = false))
        val grid = createTestGrid { r, c ->
            when {
                r == 0 && c == 0 -> createCell(r, c, notes = initialNotes)
                r == 0 && c == 1 -> createCell(
                    r,
                    c,
                    value = targetNumber
                ) // Row conflict (also block)
                r == 1 && c == 0 -> createCell(
                    r,
                    c,
                    value = targetNumber
                ) // Col conflict (also block)
                else -> createCell(r, c)
            }
        }

        useCase(grid, cellNumber = targetNumber, cellRow = 0, cellCol = 0)

        val targetNote = getNoteFromCell(grid, 0, 0, targetNumber)
        assertNotNull(targetNote)
        assertTrue(
            "Note for $targetNumber should be an error due to multiple conflicts",
            targetNote!!.isError
        )
    }

    @Test
    fun `invoke only affects target note not other notes in the same cell`() =
        runTest(testDispatcher) {
            val targetNumber = 5
            val otherNoteNumber = 3
            val initialNotes = setOf(
                SudokuCellNote(targetNumber, isError = false),
                SudokuCellNote(otherNoteNumber, isError = false)
            )
            val grid = createTestGrid { r, c ->
                when {
                    r == 0 && c == 0 -> createCell(r, c, notes = initialNotes)
                    r == 1 && c == 1 -> createCell(
                        r,
                        c,
                        value = targetNumber
                    ) // Conflict for targetNumber
                    else -> createCell(r, c)
                }
            }

            useCase(grid, cellNumber = targetNumber, cellRow = 0, cellCol = 0)

            val targetNote = getNoteFromCell(grid, 0, 0, targetNumber)
            assertNotNull(targetNote)
            assertTrue("Target note for $targetNumber should be an error", targetNote!!.isError)

            val otherNote = getNoteFromCell(grid, 0, 0, otherNoteNumber)
            assertNotNull(otherNote)
            assertFalse(
                "Other note for $otherNoteNumber should NOT be an error",
                otherNote!!.isError
            )
        }

    @Test
    fun `invoke with existing error on note keeps it error if conflict still exists`() =
        runTest(testDispatcher) {
            val targetNumber = 5
            val initialNotes =
                setOf(SudokuCellNote(targetNumber, isError = true)) // Note is already an error
            val grid = createTestGrid { r, c ->
                when {
                    r == 0 && c == 0 -> createCell(r, c, notes = initialNotes)
                    r == 1 && c == 1 -> createCell(
                        r,
                        c,
                        value = targetNumber
                    ) // Conflict in same block
                    else -> createCell(r, c)
                }
            }

            useCase(grid, cellNumber = targetNumber, cellRow = 0, cellCol = 0)

            val targetNote = getNoteFromCell(grid, 0, 0, targetNumber)
            assertNotNull(targetNote)
            assertTrue("Note for $targetNumber should remain an error", targetNote!!.isError)
        }

    // This test assumes that if a note was an error, but the conflict is removed,
    // the use case *does not* clear the error. The current code only sets isError = true.
    // If the requirement is to also clear errors, the use case logic needs adjustment.
    @Test
    fun `invoke does not clear existing error on note if no conflict is found for it now`() =
        runTest(testDispatcher) {
            val targetNumber = 5
            val initialNotes =
                setOf(SudokuCellNote(targetNumber, isError = true)) // Note is an error
            val grid = createTestGrid { r, c ->
                if (r == 0 && c == 0) createCell(r, c, notes = initialNotes) else createCell(r, c)
            }
            // No conflicts for targetNumber on the grid

            useCase(grid, cellNumber = targetNumber, cellRow = 0, cellCol = 0)

            val targetNote = getNoteFromCell(grid, 0, 0, targetNumber)
            assertNotNull(targetNote)
            assertTrue(
                "Note for $targetNumber should remain an error (current logic only sets true)",
                targetNote!!.isError
            )
        }
}