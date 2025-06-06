package org.dsh.personal.sudoku.domain.useCase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dsh.personal.sudoku.domain.BLOCK_SIZE
import org.dsh.personal.sudoku.domain.ROW_SIZE
import org.dsh.personal.sudoku.domain.entity.SudokuCellNote
import org.dsh.personal.sudoku.domain.entity.SudokuCellState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ValidateBoardUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: ValidateBoardUseCase

    private val testRowSize = ROW_SIZE
    private val testBlockSize = BLOCK_SIZE

    @Before
    fun setUp() {
        useCase = ValidateBoardUseCase(testDispatcher)
        assertEquals("Test ROW_SIZE must match domain ROW_SIZE", ROW_SIZE, testRowSize)
        assertEquals("Test BLOCK_SIZE must match domain BLOCK_SIZE", BLOCK_SIZE, testBlockSize)
    }

    private fun createCell(
        row: Int,
        col: Int,
        value: Int = 0,
        isError: Boolean = false,
        notes: Set<SudokuCellNote> = emptySet()
    ): SudokuCellState {
        return SudokuCellState(
            id = "${row}_${col}",
            value = value,
            isError = isError,
            notes = notes,
            isClue = true
        )
    }

    private fun createTestGrid(
        rows: Int = testRowSize,
        cols: Int = testRowSize,
        initialCellSetup: (r: Int, c: Int) -> SudokuCellState = { r, c -> createCell(r, c) }
    ): List<List<SudokuCellState>> {
        return List(rows) { r ->
            List(cols) { c ->
                initialCellSetup(r, c)
            }
        }
    }

    private fun getNoteVisibility(notes: Set<SudokuCellNote>, number: Int): Boolean? {
        return notes.find { it.value == number }?.isVisible
    }

    @Test
    fun `invoke clears existing errors before new validation`() = runTest(testDispatcher) {
        val grid = createTestGrid { r, c ->
            createCell(r, c, value = if (r == 0 && c == 0) 1 else 0, isError = true)
        }
        // Initially, cell (0,0) has an error. The grid is otherwise valid.
        assertTrue(grid[0][0].isError)

        // Invoke with parameters that don't introduce new errors
        useCase(grid, cellNumber = 2, cellRow = 1, cellCol = 1)

        // All errors should be cleared if no new validation errors are found
        grid.flatten().forEach { cell ->
            assertFalse("Cell ${cell.id} should not have an error", cell.isError)
        }
    }

    @Test
    fun `invoke makes notes visible for cellNumber when target cell is empty`() =
        runTest(testDispatcher) {
            val targetRow = 0
            val targetCol = 0
            val effectingNumber = 5

            val grid = createTestGrid { r, c ->
                val initialNotes = if ((r == targetRow && c == 1) || // same row
                    (r == 1 && c == targetCol) || // same col
                    (r == 1 && c == 1)            // same block
                ) {
                    setOf(
                        SudokuCellNote(effectingNumber, isVisible = false),
                        SudokuCellNote(3, isVisible = true)
                    )
                } else {
                    setOf(SudokuCellNote(effectingNumber, isVisible = false))
                }
                createCell(r, c, value = 0, notes = initialNotes) // All cells empty
            }

            // Target cell (0,0) is empty.
            assertEquals(0, grid[targetRow][targetCol].value)

            useCase(grid, cellNumber = effectingNumber, cellRow = targetRow, cellCol = targetCol)

            // Notes for 'effectingNumber' should become visible in related empty cells
            // Cell in same row & block
            assertTrue(getNoteVisibility(grid[0][1].notes, effectingNumber)!!)
            assertTrue(getNoteVisibility(grid[0][1].notes, 3)!!) // Other notes untouched

            // Cell in same col & block
            assertTrue(getNoteVisibility(grid[1][0].notes, effectingNumber)!!)
            // Cell in same block
            assertTrue(getNoteVisibility(grid[1][1].notes, effectingNumber)!!)

            // Cell in same row, different block
            assertTrue(getNoteVisibility(grid[0][testRowSize - 1].notes, effectingNumber)!!)
            // Cell in same col, different block
            assertTrue(getNoteVisibility(grid[testRowSize - 1][0].notes, effectingNumber)!!)

            // Cell not in same row/col/block - its notes for effectingNumber should remain false
            if (testRowSize > testBlockSize && testRowSize > testBlockSize) { // Avoid index out of bounds for small grids
                assertFalse(
                    getNoteVisibility(
                        grid[testBlockSize][testBlockSize].notes,
                        effectingNumber
                    )!!
                )
            }
        }


    @Test
    fun `invoke makes notes invisible for cellNumber when target cell has value`() =
        runTest(testDispatcher) {
            val targetRow = 0
            val targetCol = 0
            val effectingNumber = 5

            val grid = createTestGrid { r, c ->
                val cellValue =
                    if (r == targetRow && c == targetCol) effectingNumber else 0 // Target cell has the value
                val initialNotes = if ((r == targetRow && c == 1 && cellValue == 0) ||
                    (r == 1 && c == targetCol && cellValue == 0) ||
                    (r == 1 && c == 1 && cellValue == 0)
                ) {
                    setOf(
                        SudokuCellNote(effectingNumber, isVisible = true),
                        SudokuCellNote(3, isVisible = false)
                    )
                } else {
                    setOf(SudokuCellNote(effectingNumber, isVisible = true))
                }
                createCell(r, c, value = cellValue, notes = initialNotes)
            }
            // Target cell (0,0) has a value.
            assertEquals(effectingNumber, grid[targetRow][targetCol].value)

            useCase(grid, cellNumber = effectingNumber, cellRow = targetRow, cellCol = targetCol)

            // Notes for 'effectingNumber' should become invisible in related empty cells
            assertFalse(getNoteVisibility(grid[0][1].notes, effectingNumber)!!) // Same row, empty
            assertFalse(getNoteVisibility(grid[0][1].notes, 3)!!) // Other notes untouched

            assertFalse(getNoteVisibility(grid[1][0].notes, effectingNumber)!!) // Same col, empty
            assertFalse(getNoteVisibility(grid[1][1].notes, effectingNumber)!!) // Same block, empty
        }

    @Test
    fun `invoke with valid grid sets no errors`() = runTest(testDispatcher) {
        val intGrid = listOf(
            listOf(1, 2, 3, 4, 5, 6, 7, 8, 9),
            listOf(4, 5, 6, 7, 8, 9, 1, 2, 3),
            listOf(7, 8, 9, 1, 2, 3, 4, 5, 6),
            listOf(2, 1, 4, 3, 6, 5, 8, 9, 7),
            listOf(3, 6, 5, 8, 9, 7, 2, 1, 4),
            listOf(8, 9, 7, 2, 1, 4, 3, 6, 5),
            listOf(5, 3, 1, 6, 4, 2, 9, 7, 8),
            listOf(6, 4, 2, 9, 7, 8, 5, 3, 1),
            listOf(9, 7, 8, 5, 3, 1, 6, 4, 2)
        )
        val grid = createTestGrid { r, c -> createCell(r, c, value = intGrid[r][c]) }

        useCase(grid, 1, 0, 0) // Parameters shouldn't matter for error state of a valid grid

        grid.flatten().forEach { cell ->
            assertFalse(
                "Cell ${cell.id} (value ${cell.value}) should have no error in a valid grid.",
                cell.isError
            )
        }
    }

    @Test
    fun `invoke sets errors for row duplicates`() = runTest(testDispatcher) {
        val grid = createTestGrid { r, c ->
            val value = when {
                r == 0 && c == 0 -> 5 // Duplicate
                r == 0 && c == testRowSize - 1 -> 5 // Duplicate
                r == 0 && c == 1 -> 1 // Non-duplicate
                else -> 0
            }
            createCell(r, c, value = value)
        }

        useCase(grid, 5, 0, 0)

        assertTrue(grid[0][0].isError)
        assertFalse(grid[0][1].isError) // Cell with 1 should not be an error
        assertTrue(grid[0][testRowSize - 1].isError)
    }

    @Test
    fun `invoke sets errors for column duplicates`() = runTest(testDispatcher) {
        val grid = createTestGrid { r, c ->
            val value = when {
                r == 0 && c == 0 -> 5 // Duplicate
                r == testRowSize - 1 && c == 0 -> 5 // Duplicate
                r == 1 && c == 0 -> 1 // Non-duplicate
                else -> 0
            }
            createCell(r, c, value = value)
        }
        useCase(grid, 5, 0, 0)

        assertTrue(grid[0][0].isError)
        assertFalse(grid[1][0].isError) // Cell with 1
        assertTrue(grid[testRowSize - 1][0].isError)
    }

    @Test
    fun `invoke sets errors for block duplicates`() = runTest(testDispatcher) {
        val grid = createTestGrid { r, c ->
            val value = when {
                r == 0 && c == 0 -> 5 // Duplicate
                r == 1 && c == 1 -> 5 // Duplicate in same block
                r == 0 && c == 1 -> 1 // Non-duplicate in same block
                else -> 0
            }
            createCell(r, c, value = value)
        }
        useCase(grid, 5, 0, 0)

        assertTrue(grid[0][0].isError)
        assertTrue(grid[1][1].isError)
        assertFalse(grid[0][1].isError) // Cell with 1
    }

    @Test
    fun `invoke marks all occurrences of a duplicate number in a row`() = runTest(testDispatcher) {
        val grid = createTestGrid { r, c ->
            val value =
                if (r == 0 && (c == 0 || c == 2 || c == 4)) 5 else if (r == 0 && c == 1) 1 else 0
            createCell(r, c, value = value)
        }
        // Row 0: [5, 1, 5, 0, 5, 0, 0, 0, 0]

        useCase(grid, 5, 0, 0)

        assertTrue("Cell 0,0 (value 5) should be error", grid[0][0].isError)
        assertFalse("Cell 0,1 (value 1) should not be error", grid[0][1].isError)
        assertTrue("Cell 0,2 (value 5) should be error", grid[0][2].isError)
        assertFalse("Cell 0,3 (value 0) should not be error", grid[0][3].isError)
        assertTrue("Cell 0,4 (value 5) should be error", grid[0][4].isError)
    }
}