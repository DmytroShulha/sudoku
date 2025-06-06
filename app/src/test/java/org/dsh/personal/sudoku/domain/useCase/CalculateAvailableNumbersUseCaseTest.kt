package org.dsh.personal.sudoku.domain.useCase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dsh.personal.sudoku.domain.ROW_SIZE
import org.dsh.personal.sudoku.domain.entity.SudokuNumberButtonState
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class CalculateAvailableNumbersUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: CalculateAvailableNumbersUseCase

    @Before
    fun setUp() {
        useCase = CalculateAvailableNumbersUseCase(testDispatcher)
    }

    private fun createEmptyGrid(): List<List<Int>> {
        return List(ROW_SIZE) { List(ROW_SIZE) { 0 } }
    }

    private fun createFullSolvedGrid(): List<List<Int>> {
        // A simple valid solved Sudoku grid for testing
        return listOf(
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
    }

    @Test
    fun `invoke with empty grid returns all numbers available`() = runTest(testDispatcher) {
        val emptyGrid = createEmptyGrid()
        // Corrected to use isPossible and availableCount in expected state creation if needed,
        // but the constructor SudokuNumberButtonState(number, isPossible, availableCount) is used directly.
        val expectedStates = (1..ROW_SIZE).map { SudokuNumberButtonState(it, true, ROW_SIZE) }

        val result = useCase(emptyGrid)

        assertEquals(expectedStates.size, result.size)
        result.forEach { buttonState ->
            val expected = expectedStates.find { it.number == buttonState.number }
            assertEquals(expected?.isPossible, buttonState.isPossible)
            assertEquals(expected?.availableCount, buttonState.availableCount)
            assertTrue("For number ${buttonState.number}, isPossible should be true", buttonState.isPossible)
            assertEquals("For number ${buttonState.number}, availableCount should be $ROW_SIZE", ROW_SIZE, buttonState.availableCount)
        }
    }

    @Test
    fun `invoke with full grid returns no numbers available`() = runTest(testDispatcher) {
        val fullGrid = createFullSolvedGrid()
        val expectedStates = (1..ROW_SIZE).map { SudokuNumberButtonState(it, false, 0) }

        val result = useCase(fullGrid)

        assertEquals(expectedStates.size, result.size)
        result.forEach { buttonState ->
            val expected = expectedStates.find { it.number == buttonState.number }
            assertEquals(expected?.isPossible, buttonState.isPossible)
            assertEquals(expected?.availableCount, buttonState.availableCount)
            assertFalse("For number ${buttonState.number}, isPossible should be false", buttonState.isPossible)
            assertEquals("For number ${buttonState.number}, availableCount should be 0", 0, buttonState.availableCount)
        }
    }

    @Test
    fun `invoke with partially filled grid returns correct availability`() = runTest(testDispatcher) {
        val partialGrid = createEmptyGrid().toMutableList().map { it.toMutableList() }
        // Fill some numbers:
        // Number 1 appears twice
        partialGrid[0][0] = 1
        partialGrid[1][1] = 1
        // Number 5 appears once
        partialGrid[2][2] = 5
        // Number 9 appears three times
        partialGrid[3][3] = 9
        partialGrid[4][4] = 9
        partialGrid[5][5] = 9

        val result = useCase(partialGrid)

        val stateFor1 = result.find { it.number == 1 }
        Assert.assertNotNull(stateFor1) // Using Assert.assertNotNull
        assertTrue(stateFor1!!.isPossible)
        assertEquals(ROW_SIZE - 2, stateFor1.availableCount)

        val stateFor2 = result.find { it.number == 2 }
        Assert.assertNotNull(stateFor2)
        assertTrue(stateFor2!!.isPossible)
        assertEquals(ROW_SIZE, stateFor2.availableCount)

        val stateFor5 = result.find { it.number == 5 }
        Assert.assertNotNull(stateFor5)
        assertTrue(stateFor5!!.isPossible)
        assertEquals(ROW_SIZE - 1, stateFor5.availableCount)

        val stateFor9 = result.find { it.number == 9 }
        Assert.assertNotNull(stateFor9)
        assertTrue(stateFor9!!.isPossible)
        assertEquals(ROW_SIZE - 3, stateFor9.availableCount)

        // Check if a number becomes fully used
        val fullUseGrid = createEmptyGrid().toMutableList().map { it.toMutableList() }
        for(i in 0 until ROW_SIZE) fullUseGrid[0][i] = 7 // Number 7 appears 9 times

        val resultFullUse = useCase(fullUseGrid)
        val stateFor7 = resultFullUse.find { it.number == 7 }
        Assert.assertNotNull(stateFor7)
        assertFalse(stateFor7!!.isPossible)
        assertEquals(0, stateFor7.availableCount)
    }

    @Test
    fun `invoke with invalid grid size (too few rows) returns empty list`() = runTest(testDispatcher) {
        val invalidGrid = List(ROW_SIZE - 1) { List(ROW_SIZE) { 0 } }
        val result = useCase(invalidGrid)
        assertTrue("Result should be an empty list for invalid grid size", result.isEmpty())
    }

    @Test
    fun `invoke with invalid grid size (too few columns) returns empty list`() = runTest(testDispatcher) {
        val invalidGrid = List(ROW_SIZE) { List(ROW_SIZE - 1) { 0 } }
        val result = useCase(invalidGrid)
        assertTrue("Result should be an empty list for invalid grid size", result.isEmpty())
    }

    @Test
    fun `invoke with grid containing invalid numbers (0 or greater than 9) ignores them`() = runTest(testDispatcher) {
        val gridWithInvalidNumbers = createEmptyGrid().toMutableList().map { it.toMutableList() }
        gridWithInvalidNumbers[0][0] = 1 // Valid
        gridWithInvalidNumbers[0][1] = 0  // Invalid, should be ignored
        gridWithInvalidNumbers[0][2] = 10 // Invalid, should be ignored
        gridWithInvalidNumbers[0][3] = -5 // Invalid, should be ignored
        gridWithInvalidNumbers[1][0] = 2 // Valid

        val result = useCase(gridWithInvalidNumbers)

        val stateFor1 = result.find { it.number == 1 }
        Assert.assertNotNull(stateFor1)
        assertTrue(stateFor1!!.isPossible)
        assertEquals("Only one '1' should be counted", ROW_SIZE - 1, stateFor1.availableCount)

        val stateFor2 = result.find { it.number == 2 }
        Assert.assertNotNull(stateFor2)
        assertTrue(stateFor2!!.isPossible)
        assertEquals("Only one '2' should be counted", ROW_SIZE - 1, stateFor2.availableCount)

        // Ensure other numbers are fully available
        val stateFor3 = result.find { it.number == 3 }
        Assert.assertNotNull(stateFor3)
        assertTrue(stateFor3!!.isPossible)
        assertEquals(ROW_SIZE, stateFor3.availableCount)
    }
}