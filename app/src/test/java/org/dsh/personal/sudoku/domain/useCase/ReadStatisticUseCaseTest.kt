package org.dsh.personal.sudoku.domain.useCase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.dsh.personal.sudoku.domain.entity.DifficultyStats
import org.dsh.personal.sudoku.domain.entity.SudokuGameStats
import org.dsh.personal.sudoku.domain.repository.SudokuRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ReadStatisticUseCaseTest {

    private lateinit var mockRepository: SudokuRepository
    private lateinit var readStatisticUseCase: ReadStatisticUseCase

    @Before
    fun setUp() {
        mockRepository = mockk()
        readStatisticUseCase = ReadStatisticUseCase(mockRepository)
    }

    @Test
    fun `invoke calls repository readStatistic and returns its result`() = runTest {
        // Arrange
        val expectedStats = SudokuGameStats(
            totalGamesPlayed = 10,
            totalGamesWon = 5,
            easyStats = DifficultyStats(
                gamesPlayed = 3,
                gamesWon = 2,
                averageCompletionTimeMillis = 120000L,
                fastestCompletionTimeMillis = 90000L
            ),
            mediumStats = DifficultyStats(
                gamesPlayed = 4,
                gamesWon = 2,
                averageCompletionTimeMillis = 180000L,
                fastestCompletionTimeMillis = 150000L
            ),
            hardStats = DifficultyStats(
                gamesPlayed = 2,
                gamesWon = 1,
                averageCompletionTimeMillis = 240000L,
                fastestCompletionTimeMillis = 200000L
            ),
            expertStats = DifficultyStats(
                gamesPlayed = 1,
                gamesWon = 0,
                averageCompletionTimeMillis = 0L,
                fastestCompletionTimeMillis = Long.MAX_VALUE
            )
        )
        coEvery { mockRepository.readStatistic() } returns expectedStats

        // Act
        val resultStats = readStatisticUseCase() // Invokes the use case

        // Assert
        // Verify readStatistic was called on the repository
        coVerify(exactly = 1) { mockRepository.readStatistic() }

        // Verify the result is the one returned by the repository
        assertEquals(
            "The returned game stats should match the one from the repository.",
            expectedStats,
            resultStats
        )
    }

    @Test
    fun `invoke with empty stats from repository returns empty stats`() = runTest {
        // Arrange
        val emptyStats =
            SudokuGameStats( // Assuming default constructor creates empty/default stats
                totalGamesPlayed = 0,
                totalGamesWon = 0,
                easyStats = DifficultyStats(0, 0, 0L, Long.MAX_VALUE),
                mediumStats = DifficultyStats(0, 0, 0L, Long.MAX_VALUE),
                hardStats = DifficultyStats(0, 0, 0L, Long.MAX_VALUE),
                expertStats = DifficultyStats(0, 0, 0L, Long.MAX_VALUE)
            )
        coEvery { mockRepository.readStatistic() } returns emptyStats

        // Act
        val resultStats = readStatisticUseCase()

        // Assert
        coVerify(exactly = 1) { mockRepository.readStatistic() }
        assertEquals(emptyStats, resultStats)
    }
}