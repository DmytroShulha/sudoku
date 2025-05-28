package org.dsh.personal.sudoku.data

import kotlinx.coroutines.Dispatchers
import org.dsh.personal.sudoku.domain.repository.SudokuGenerator
import org.dsh.personal.sudoku.domain.entity.Difficulty
import kotlin.random.Random

class SudokuGeneratorEasy : SudokuGenerator {

    private val gridSize = 9
    private val subgridSize = 3
    private var currentSolutionCount = 0 // Used by the solution counting mechanism

    /**
     * Generates a Sudoku puzzle as a 9x9 array of Ints.
     * '0' represents an empty cell.
     *
     * @param difficulty The desired difficulty level of the puzzle.
     * @return A 9x9 Array<IntArray> representing the Sudoku puzzle.
     */
    override suspend fun generate(difficulty: Difficulty): Pair<Array<IntArray>, Array<IntArray>> = with(Dispatchers.Default) {
        val solution = generateFullSolution()
        solution to createPuzzleFromSolution(solution, difficulty)
    }

    @Suppress("unused")
    fun printGrid(grid: Array<IntArray>) {
        println("+-------+-------+-------+")
        for (i in grid.indices) {
            print("| ")
            for (j in grid[i].indices) {
                print(if (grid[i][j] == 0) ". " else "${grid[i][j]} ")
                if ((j + 1) % 3 == 0) print("| ")
            }
            println()
            if ((i + 1) % 3 == 0) {
                println("+-------+-------+-------+")
            }
        }
        println()
    }

    /**
     * Generates a fully solved Sudoku grid.
     */
    private suspend fun generateFullSolution(): Array<IntArray> {
        val grid = Array(gridSize) { IntArray(gridSize) { 0 } }
        solve(grid)
        return grid
    }

    /**
     * Recursive backtracking algorithm to fill an empty Sudoku grid.
     *
     * @param grid The grid to solve.
     * @return True if a solution was found, false otherwise.
     */
    private suspend fun solve(grid: Array<IntArray>): Boolean {
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (grid[row][col] == 0) { // Find an empty cell
                    val numbers = (1..gridSize).shuffled(Random) // Try numbers in random order
                    for (num in numbers) {
                        if (isValidPlacement(grid, num, row, col)) {
                            grid[row][col] = num
                            if (solve(grid)) {
                                return true // Solution found
                            }
                            grid[row][col] = 0 // Backtrack
                        }
                    }
                    return false // No valid number found for this empty cell, trigger backtrack
                }
            }
        }
        return true // All cells are filled, solution complete
    }

    /**
     * Removes numbers from a full Sudoku solution to create a puzzle of a given difficulty.
     * Ensures the resulting puzzle has a unique solution.
     *
     * @param solution A fully solved Sudoku grid.
     * @param difficulty The target difficulty for the puzzle.
     * @return A 9x9 Array<IntArray> representing the puzzle with some cells empty (0).
     */
    private suspend fun createPuzzleFromSolution(solution: Array<IntArray>, difficulty: Difficulty): Array<IntArray> {
        val puzzle = solution.map { it.clone() }.toTypedArray()

        // Determine the number of cells to try to remove based on difficulty.
        // These are targets; the actual number removed might be less if uniqueness is compromised.
        // Values aim to leave a certain number of clues (81 - cellsToAttemptToRemove).
        val cellsToAttemptToRemove = when (difficulty) {
            Difficulty.EASY -> 40   // Leaves ~41 clues
            Difficulty.MEDIUM -> 48  // Leaves ~33 clues
            Difficulty.HARD -> 54   // Leaves ~27 clues
            Difficulty.EXPERT -> 58  // Leaves ~23 clues (can be very hard to generate uniquely)
        }

        val cellCoordinates = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                cellCoordinates.add(Pair(r, c))
            }
        }
        cellCoordinates.shuffle(Random) // Randomize the order of cell removal attempts

        var cellsRemovedSoFar = 0
        for ((row, col) in cellCoordinates) {
            if (cellsRemovedSoFar >= cellsToAttemptToRemove) {
                break // Reached the target number of removals for this difficulty
            }

            if (puzzle[row][col] == 0) {
                continue // Cell already empty (shouldn't happen if starting from full solution)
            }

            val originalValue = puzzle[row][col]
            puzzle[row][col] = 0 // Attempt to remove the number

            // Check if the puzzle still has a unique solution
            val numberOfSolutions = countSolutionsForGrid(puzzle.map { it.clone() }.toTypedArray())

            if (numberOfSolutions != 1) {
                // If not unique (or no solution), revert the change
                puzzle[row][col] = originalValue
            } else {
                // Removal was successful (puzzle remains uniquely solvable)
                cellsRemovedSoFar++
            }
        }
        return puzzle
    }

    /**
     * Counts the number of solutions for a given Sudoku grid.
     *
     * @param grid The Sudoku grid to check.
     * @return The number of unique solutions (e.g., 0, 1, or 2+).
     */
    private suspend fun countSolutionsForGrid(grid: Array<IntArray>): Int {
        currentSolutionCount = 0
        // We only need to know if there's 0, 1, or >1 solutions.
        // So, we can stop counting if we find 2 solutions.
        internalSolveAndCount(grid.map { it.clone() }.toTypedArray(), 2)
        return currentSolutionCount
    }

    /**
     * Recursive helper function to find and count solutions up to a specified limit.
     * Modifies `this.currentSolutionCount`.
     *
     * @param grid The grid to solve/analyze.
     * @param countLimit Stop counting once this many solutions are found.
     */
    private suspend fun internalSolveAndCount(grid: Array<IntArray>, countLimit: Int) {
        if (currentSolutionCount >= countLimit) {
            return // Already found enough solutions, no need to search further
        }

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (grid[row][col] == 0) { // Find an empty cell
                    for (num in 1..gridSize) { // Try numbers 1-9
                        if (isValidPlacement(grid, num, row, col)) {
                            grid[row][col] = num
                            internalSolveAndCount(grid, countLimit)
                            if (currentSolutionCount >= countLimit) {
                                return // Propagate early exit
                            }
                            grid[row][col] = 0 // Backtrack
                        }
                    }
                    return // No valid number for this cell, backtrack from previous call
                }
            }
        }
        // All cells are filled, a solution is found
        currentSolutionCount++
    }

    /**
     * Checks if placing a given number in a specific cell is valid according to Sudoku rules.
     *
     * @param grid The current Sudoku grid.
     * @param number The number to check.
     * @param row The row index of the cell.
     * @param col The column index of the cell.
     * @return True if the placement is valid, false otherwise.
     */
    @Suppress("RedundantSuspendModifier")
    private suspend fun isValidPlacement(grid: Array<IntArray>, number: Int, row: Int, col: Int): Boolean {
        // Check row
        for (c in 0 until gridSize) {
            if (grid[row][c] == number) return false
        }
        // Check column
        for (r in 0 until gridSize) {
            if (grid[r][col] == number) return false
        }
        // Check 3x3 subgrid
        val subgridRowStart = row - row % subgridSize
        val subgridColStart = col - col % subgridSize
        for (r in subgridRowStart until subgridRowStart + subgridSize) {
            for (c in subgridColStart until subgridColStart + subgridSize) {
                if (grid[r][c] == number) return false
            }
        }
        return true // Placement is valid
    }
}

