package org.dsh.personal.sudoku.domain

import org.dsh.personal.sudoku.domain.entity.SudokuCellState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuHelperTest {

    private fun createCell(
        id: String,
        value: Int = 0,
        isClue: Boolean = false,
        isError: Boolean = false
    ): SudokuCellState {
        return SudokuCellState(id = id, value = value, isClue = isClue, isError = isError)
    }

    // --- Tests for isSameRow, isSameCol, isSameBlock ---

    @Test
    fun `isSameRow returns true for same rows`() {
        assertTrue(isSameRow(0, 0))
        assertTrue(isSameRow(5, 5))
    }

    @Test
    fun `isSameRow returns false for different rows`() {
        assertFalse(isSameRow(0, 1))
        assertFalse(isSameRow(5, 8))
    }

    @Test
    fun `isSameCol returns true for same columns`() {
        assertTrue(isSameCol(0, 0))
        assertTrue(isSameCol(3, 3))
    }

    @Test
    fun `isSameCol returns false for different columns`() {
        assertFalse(isSameCol(0, 1))
        assertFalse(isSameCol(3, 7))
    }

    @Test
    fun `isSameBlock returns true for cells in the same block`() {
        // Top-left block (0,0) to (2,2)
        assertTrue(isSameBlock(0, 0, 1, 1)) // Same cell essentially for block check
        assertTrue(isSameBlock(0, 0, 2, 2))
        assertTrue(isSameBlock(1, 1, 0, 2))
        // Middle block (3,3) to (5,5)
        assertTrue(isSameBlock(3, 3, 4, 5))
        assertTrue(isSameBlock(5, 4, 3, 3))
    }

    @Test
    fun `isSameBlock returns false for cells in different blocks`() {
        // Same row, different block
        assertFalse(isSameBlock(0, 0, 0, 3)) // (0,0) vs (0,3)
        // Same col, different block
        assertFalse(isSameBlock(0, 0, 3, 0)) // (0,0) vs (3,0)
        // Different row, different col, different block
        assertFalse(isSameBlock(0, 0, 4, 4)) // (0,0) vs (4,4)
        assertFalse(isSameBlock(2, 2, 3, 3)) // Edge case: (2,2) in block 0,0 ; (3,3) in block 1,1
    }

    // --- Tests for markDuplicatesInRow ---

    @Test
    fun `markDuplicatesInRow no duplicate`() {
        val seen = mutableSetOf<Int>()
        val cell = createCell("c1", 5)
        val row = listOf(createCell("c0", 1), cell, createCell("c2", 2))

        markDuplicatesInRow(seen, cell, 1, row)

        assertFalse(cell.isError)
        assertFalse(row[0].isError)
    }

    @Test
    fun `markDuplicatesInRow with duplicate marks errors`() {
        val seen = mutableSetOf(5) // 5 is already seen
        val prevCell = createCell("c0", 5, isError = false)
        val currentCell = createCell("c1", 5, isError = false)
        val row = listOf(prevCell, currentCell, createCell("c2", 2))

        markDuplicatesInRow(seen, currentCell, 1, row)

        assertTrue(currentCell.isError)
        assertTrue(prevCell.isError) // Previous occurrence should also be marked
        assertFalse(row[2].isError)
    }

    @Test
    fun `markDuplicatesInRow with multiple previous duplicates marks all errors`() {
        val seen = mutableSetOf(5)
        val prevCell1 = createCell("c0", 5, isError = false)
        val prevCell2 = createCell("c2", 5, isError = false)
        val currentCell = createCell("c4", 5, isError = false)
        val row =
            listOf(prevCell1, createCell("c1", 1), prevCell2, createCell("c3", 2), currentCell)

        markDuplicatesInRow(seen, currentCell, 4, row)

        assertTrue(currentCell.isError)
        assertTrue(prevCell1.isError)
        assertTrue(prevCell2.isError)
        assertFalse(row[1].isError)
        assertFalse(row[3].isError)
    }


    @Test
    fun `markDuplicatesInRow with cell value 0 does nothing`() {
        val seen = mutableSetOf(1)
        val cell = createCell("c1", 0) // Empty cell
        val row = listOf(createCell("c0", 1), cell, createCell("c2", 2))
        val originalSeenSize = seen.size

        markDuplicatesInRow(seen, cell, 1, row)

        assertFalse(cell.isError)
        assertEquals(originalSeenSize, seen.size) // Seen set should not change for value 0
    }

    // --- Tests for markDuplicateErrorsInSubgrid (and implicitly markDuplicatesInBlock) ---

    private fun createGridForSubgridTest(): List<List<SudokuCellState>> {
        return List(ROW_SIZE) { r -> List(ROW_SIZE) { c -> createCell("${r}_${c}") } }
    }

    @Test
    fun `markDuplicateErrorsInSubgrid no duplicate in subgrid`() {
        val seen = mutableSetOf<Int>()
        val grid = createGridForSubgridTest()
        grid[0][0].value = 5
        val param = MarkDuplicatesInBlockParam(0, 0, 0, 0, grid, grid[0][0])

        markDuplicateErrorsInSubgrid(seen, param)

        assertFalse(grid[0][0].isError)
        assertTrue(seen.contains(5))
    }

    @Test
    fun `markDuplicateErrorsInSubgrid with duplicate marks errors in subgrid`() {
        val seen = mutableSetOf(5) // 5 is already seen from a previous cell in the subgrid scan
        val grid = createGridForSubgridTest()
        grid[0][0].value = 5 // First occurrence
        grid[1][1].value = 5 // Current cell, duplicate

        // Simulating that grid[0][0] was processed and 5 added to seen
        // Now processing grid[1][1]
        val param = MarkDuplicatesInBlockParam(0, 0, 1, 1, grid, grid[1][1])

        markDuplicateErrorsInSubgrid(seen, param)

        assertTrue(grid[1][1].isError) // Current cell marked
        assertTrue(grid[0][0].isError) // Previous cell in block marked by markDuplicatesInBlock
    }

    @Test
    fun `markDuplicateErrorsInSubgrid with duplicate in subgrid but different from param cell value`() {
        val seen = mutableSetOf<Int>() // Start fresh
        val grid = createGridForSubgridTest()
        grid[0][0].value =
            5 // This one is already "placed" and caused the "seen" for current iteration of outer loop
        grid[1][1].value =
            5 // This is the current cell being processed by outer loop, and will be added to its 'seen' set
        // and markDuplicatesInSubgrid called.

        // Simulate processing grid[0][0] first for the subgrid scan
        val param00 = MarkDuplicatesInBlockParam(0, 0, 0, 0, grid, grid[0][0])
        markDuplicateErrorsInSubgrid(seen, param00) // seen becomes {5}, grid[0][0].isError = false

        assertFalse(grid[0][0].isError)
        assertEquals(setOf(5), seen)

        // Now, the outer loop processes grid[1][1]. Its value is 5.
        // The `seen` set for the *subgrid* is what markDuplicateErrorsInSubgrid cares about.
        // Let's assume the outer loop (not part of this unit test) would have its own `seenRow`, `seenCol` sets.
        // This test focuses on the subgrid check itself.
        // We'll reset `seen` for this specific subgrid check as it would be in the actual use case (ValidateBoardUseCase)
        val subgridSeen = mutableSetOf<Int>()
        grid[0][0].isError = false // ensure it's reset for this test part

        // First cell in subgrid (0,0) with value 5
        markDuplicateErrorsInSubgrid(
            subgridSeen,
            MarkDuplicatesInBlockParam(0, 0, 0, 0, grid, grid[0][0])
        )
        assertFalse(grid[0][0].isError)
        assertEquals(setOf(5), subgridSeen)

        // Second cell in subgrid (1,1) also with value 5
        markDuplicateErrorsInSubgrid(
            subgridSeen,
            MarkDuplicatesInBlockParam(0, 0, 1, 1, grid, grid[1][1])
        )
        assertTrue(grid[1][1].isError) // Current cell is error
        assertTrue(grid[0][0].isError) // Previous cell in block is also error
    }


    @Test
    fun `markDuplicateErrorsInSubgrid with cell value 0 does nothing`() {
        val seen = mutableSetOf(1)
        val grid = createGridForSubgridTest()
        grid[0][0].value = 0 // Empty cell
        val param = MarkDuplicatesInBlockParam(0, 0, 0, 0, grid, grid[0][0])
        val originalSeenSize = seen.size

        markDuplicateErrorsInSubgrid(seen, param)

        assertFalse(grid[0][0].isError)
        assertEquals(originalSeenSize, seen.size) // Seen set should not change
    }

    // --- Tests for markDuplicatesInColumn ---

    @Test
    fun `markDuplicatesInColumn no duplicate`() {
        val seen = mutableSetOf<Int>()
        val grid = createGridForSubgridTest() // Reusing helper
        grid[1][0].value = 5 // The cell being checked
        grid[0][0].value = 1 // Another cell in the column

        markDuplicatesInColumn(seen, grid[1][0], 1, grid, 0)

        assertFalse(grid[1][0].isError)
        assertFalse(grid[0][0].isError)
    }

    @Test
    fun `markDuplicatesInColumn with duplicate marks errors`() {
        val seen = mutableSetOf(5) // 5 is already seen in this column scan
        val grid = createGridForSubgridTest()
        grid[0][0].value = 5 // Previous occurrence
        grid[1][0].value = 5 // Current cell being checked
        grid[2][0].value = 2 // Another cell

        markDuplicatesInColumn(seen, grid[1][0], 1, grid, 0)

        assertTrue(grid[1][0].isError)
        assertTrue(grid[0][0].isError) // Previous occurrence marked
        assertFalse(grid[2][0].isError)
    }

    @Test
    fun `markDuplicatesInColumn with multiple previous duplicates marks all errors`() {
        val seen = mutableSetOf(5)
        val grid = createGridForSubgridTest()
        grid[0][0].value = 5 // Previous
        grid[2][0].value = 5 // Previous
        grid[4][0].value = 5 // Current
        grid[1][0].value = 1 // Non-duplicate


        markDuplicatesInColumn(seen, grid[4][0], 4, grid, 0)

        assertTrue(grid[4][0].isError)
        assertTrue(grid[0][0].isError)
        assertTrue(grid[2][0].isError)
        assertFalse(grid[1][0].isError)
    }

    @Test
    fun `markDuplicatesInColumn with cell value 0 does nothing`() {
        val seen = mutableSetOf(1)
        val grid = createGridForSubgridTest()
        grid[0][0].value = 1
        grid[1][0].value = 0 // Empty cell being checked
        val originalSeenSize = seen.size

        markDuplicatesInColumn(seen, grid[1][0], 1, grid, 0)

        assertFalse(grid[1][0].isError)
        assertEquals(originalSeenSize, seen.size)
    }
}