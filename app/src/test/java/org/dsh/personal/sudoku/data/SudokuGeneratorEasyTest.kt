package org.dsh.personal.sudoku.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SudokuGeneratorEasyTest {

    private lateinit var generator: SudokuGeneratorEasy
    private val testDispatcher = StandardTestDispatcher()

    private val gridSize = 9
    private val subGridSize = 3

    @Before
    fun setUp() {
        generator = SudokuGeneratorEasy()
    }

    private fun assertIsSudokuGrid(grid: Array<IntArray>, messagePrefix: String = "") {
        assertEquals("$messagePrefix Grid should have $gridSize rows.", gridSize, grid.size)
        grid.forEachIndexed { index, row ->
            assertEquals("$messagePrefix Row $index should have $gridSize columns.", gridSize, row.size)
        }
    }

    private fun assertNumbersValid(grid: Array<IntArray>, allowZeros: Boolean, messagePrefix: String = "") {
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val cellValue = grid[r][c]
                if (allowZeros) {
                    assertTrue("$messagePrefix Cell ($r,$c) value $cellValue out of range [0-9].", cellValue in 0..9)
                } else {
                    assertTrue("$messagePrefix Cell ($r,$c) value $cellValue out of range [1-9].", cellValue in 1..9)
                }
            }
        }
    }

    private fun assertNoDuplicatesInSolution(solution: Array<IntArray>, messagePrefix: String = "") {
        // Check rows
        for (r in 0 until gridSize) {
            val seen = mutableSetOf<Int>()
            for (c in 0 until gridSize) {
                if (solution[r][c] != 0 && !seen.add(solution[r][c])) {
                    fail("$messagePrefix Duplicate ${solution[r][c]} in row $r.")
                }
            }
        }
        // Check columns
        for (c in 0 until gridSize) {
            val seen = mutableSetOf<Int>()
            for (r in 0 until gridSize) {
                if (solution[r][c] != 0 && !seen.add(solution[r][c])) {
                    fail("$messagePrefix Duplicate ${solution[r][c]} in column $c.")
                }
            }
        }
        // Check subgrids
        for (sr in 0 until gridSize step subGridSize) {
            for (sc in 0 until gridSize step subGridSize) {
                val seen = mutableSetOf<Int>()
                for (r in sr until sr + subGridSize) {
                    for (c in sc until sc + subGridSize) {
                        if (solution[r][c] != 0 && !seen.add(solution[r][c])) {
                            fail("$messagePrefix Duplicate ${solution[r][c]} in subgrid starting at ($sr,$sc).")
                        }
                    }
                }
            }
        }
    }

    private fun assertPuzzleIsSubsetOfSolution(puzzle: Array<IntArray>, solution: Array<IntArray>, messagePrefix: String = "Puzzle:") {
        var hasEmptyCells = false
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (puzzle[r][c] != 0) {
                    assertEquals("$messagePrefix Puzzle cell ($r,$c) should match solution if not zero.", solution[r][c], puzzle[r][c])
                } else {
                    hasEmptyCells = true
                }
            }
        }
        assertTrue("$messagePrefix Puzzle should have at least one empty cell.", hasEmptyCells)
    }

    private fun countClues(grid: Array<IntArray>): Int {
        var clues = 0
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (grid[r][c] != 0) {
                    clues++
                }
            }
        }
        return clues
    }


    private fun runGenerateTest(difficulty: Difficulty, expectedMinClues: Int, expectedMaxAttemptedRemovals: Int) = runTest(testDispatcher) {
        val (solution, puzzle) = generator.generate(difficulty, testDispatcher)

        // Validate Solution
        assertIsSudokuGrid(solution, "Solution:")
        assertNumbersValid(solution, false, "Solution:")
        assertNoDuplicatesInSolution(solution, "Solution:")

        // Validate Puzzle
        assertIsSudokuGrid(puzzle, "Puzzle:")
        assertNumbersValid(puzzle, true, "Puzzle:")
        assertPuzzleIsSubsetOfSolution(puzzle, solution, "Puzzle:")

        val clues = countClues(puzzle)

        // The number of empty cells should be less than or equal to the "DIFFICULTY_X" constant,
        // as that constant represents the number of cells *attempted* to be removed.
        // The number of clues should be at least 81 - expectedMaxAttemptedRemovals.
        // It's hard to give an exact upper bound on clues (lower on empty) due to uniqueness constraints.
        assertTrue("Clue count for $difficulty ($clues) should be >= $expectedMinClues (expected empty <= $expectedMaxAttemptedRemovals)",
            clues >= gridSize * gridSize - expectedMaxAttemptedRemovals && clues < gridSize * gridSize) // Must have some empty cells

        // Check if generation is not deterministic (simple check)
        val (_, puzzle2) = generator.generate(difficulty, testDispatcher)
        assertFalse("Generated puzzles should generally be different", puzzle.contentDeepEquals(puzzle2))
    }

    @Test
    fun `generate EASY puzzle`() {
        // DIFFICULTY_EASY = 40 (attempts to remove 40 cells, leaving ~41 clues)
        runGenerateTest(Difficulty.EASY, 81 - 40, 40)
    }

    @Test
    fun `generate MEDIUM puzzle`() {
        // DIFFICULTY_MEDIUM = 48 (attempts to remove 48 cells, leaving ~33 clues)
        runGenerateTest(Difficulty.MEDIUM, 81 - 48, 48)
    }

    @Test
    fun `generate HARD puzzle`() {
        // DIFFICULTY_HARD = 54 (attempts to remove 54 cells, leaving ~27 clues)
        runGenerateTest(Difficulty.HARD, 81 - 54, 54)
    }

    @Test
    fun `generate EXPERT puzzle`() {
        // DIFFICULTY_EXPERT = 58 (attempts to remove 58 cells, leaving ~23 clues)
        // This can be hard to generate uniquely, so number of clues might be higher than 23.
        runGenerateTest(Difficulty.EXPERT, 81 - 58, 58)
    }

    @Test
    fun `generateFullSolution produces a valid and complete Sudoku grid`() = runTest(testDispatcher) {
        // Accessing private method for testing is not ideal, but we can call generate
        // and check its solution part.
        val (solution, _) = generator.generate(Difficulty.EASY, testDispatcher) // Difficulty doesn't matter for solution part

        assertIsSudokuGrid(solution, "Full Solution:")
        assertNumbersValid(solution, false, "Full Solution:") // No zeros
        assertNoDuplicatesInSolution(solution, "Full Solution:")
    }
}