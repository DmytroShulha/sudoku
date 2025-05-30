package org.dsh.personal.sudoku.domain.useCase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.dsh.personal.sudoku.domain.ROW_SIZE
import org.dsh.personal.sudoku.domain.entity.SudokuNumberButtonState

class CalculateAvailableNumbersUseCase(private val defaultDispatcher: CoroutineDispatcher) {
    suspend operator fun invoke(grid: List<List<Int>>): List<SudokuNumberButtonState> =
        withContext(defaultDispatcher) {
            calculateAvailableNumbers(grid).map { e ->
                SudokuNumberButtonState(
                    e.key,
                    e.value != 0,
                    e.value
                )
            }
        }

    private fun calculateAvailableNumbers(puzzleGridValues: List<List<Int>>): Map<Int, Int> {
        // Validate input board size (optional but good practice)
        if (puzzleGridValues.size != ROW_SIZE || puzzleGridValues.any { it.size != ROW_SIZE }) {
            // Handle invalid board size, perhaps return an empty map or throw an exception
            return emptyMap()
        }

        // Step 1: Initialize counts for each number from 1 to 9
        val counts = mutableMapOf<Int, Int>()
        for (i in 1..ROW_SIZE) {
            counts[i] = 0
        }

        // Step 2-4: Iterate through the puzzle and count existing numbers
        for (row in puzzleGridValues) {
            for (value in row) {
                if (value in 1..ROW_SIZE) {
                    // Increment count for numbers between 1 and 9
                    counts[value] = counts.getValue(value) + 1
                }
            }
        }

        // Step 5: Calculate available counts
        val availableCounts = mutableMapOf<Int, Int>()
        for (number in 1..ROW_SIZE) {
            val existingCount = counts.getValue(number)
            val requiredCount = ROW_SIZE // In a completed 9x9 Sudoku, each number appears 9 times
            availableCounts[number] = requiredCount - existingCount
        }

        // Step 6: Return the result
        return availableCounts
    }
}
