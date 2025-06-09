package org.dsh.personal.sudoku.domain.useCase


import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.repository.SudokuGenerator
import org.dsh.personal.sudoku.domain.repository.SudokuRepository
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class GenerateGameFieldUseCaseTest {

    private lateinit var mockRepository: SudokuRepository
    private lateinit var mockSudokuGenerator: SudokuGenerator
    private lateinit var generateGameFieldUseCase: GenerateGameFieldUseCase

    @Before
    fun setUp() {
        mockRepository = mockk(relaxUnitFun = true) // relaxUnitFun for storeStatistic
        mockSudokuGenerator = mockk()
        generateGameFieldUseCase = GenerateGameFieldUseCase(mockRepository, mockSudokuGenerator)
    }

    @Test
    fun `invoke calls repository methods and returns correct grid pair`() = runTest {
        // Arrange
        val difficulty = Difficulty.EASY
        val expectedPuzzle = Array(9) { IntArray(9) { 1 } }
        val expectedSolution = Array(9) { IntArray(9) { 2 } }
        val expectedGridPair = Pair(expectedPuzzle, expectedSolution)

        // Mock repository.storeStatistic() is covered by relaxUnitFun = true
        // coEvery { mockRepository.storeStatistic() } returns Unit

        coEvery {
            mockRepository.generateGrid(mockSudokuGenerator, difficulty)
        } returns expectedGridPair

        // Act
        val resultGridPair = generateGameFieldUseCase(difficulty)

        // Assert
        // Verify storeStatistic was called
        coVerify(exactly = 1) { mockRepository.storeStatistic() }

        // Verify generateGrid was called with the correct parameters
        coVerify(exactly = 1) { mockRepository.generateGrid(mockSudokuGenerator, difficulty) }

        // Verify the result is the one returned by the repository
        assertEquals("The returned grid pair should match the one from the repository.", expectedGridPair, resultGridPair)
        assertArrayEquals("The puzzle part of the pair should match.", expectedPuzzle, resultGridPair.first)
        assertArrayEquals("The solution part of the pair should match.", expectedSolution, resultGridPair.second)
    }

    @Test
    fun `invoke with different difficulty calls repository correctly`() = runTest {
        // Arrange
        val difficulty = Difficulty.HARD
        val expectedPuzzle = Array(9) { IntArray(9) { 3 } }
        val expectedSolution = Array(9) { IntArray(9) { 4 } }
        val expectedGridPair = Pair(expectedPuzzle, expectedSolution)

        coEvery {
            mockRepository.generateGrid(mockSudokuGenerator, difficulty)
        } returns expectedGridPair

        // Act
        val resultGridPair = generateGameFieldUseCase(difficulty)

        // Assert
        coVerify(exactly = 1) { mockRepository.storeStatistic() } // Called again in this test
        coVerify(exactly = 1) { mockRepository.generateGrid(mockSudokuGenerator, difficulty) }
        assertEquals(expectedGridPair, resultGridPair)
    }
}