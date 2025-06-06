package org.dsh.personal.sudoku.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.dsh.personal.sudoku.data.database.EntryDao
import org.dsh.personal.sudoku.data.database.entiry.StatisticEntry
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.repository.SudokuGenerator
import org.dsh.personal.sudoku.utility.initializeEmptyGame
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class SudokuGameRepositoryTest {

    private lateinit var mockGameStorage: CurrentGameStorage
    private lateinit var mockEntryDao: EntryDao
    private lateinit var mockSudokuGenerator: SudokuGenerator
    private lateinit var testIoDispatcher: TestDispatcher
    private lateinit var testDefaultDispatcher: TestDispatcher

    private lateinit var repository: SudokuGameRepository



    @Before
    fun setUp() {
        mockGameStorage = mockk(relaxUnitFun = true)
        mockEntryDao = mockk(relaxUnitFun = true)
        mockSudokuGenerator = mockk()
        testIoDispatcher = StandardTestDispatcher()
        testDefaultDispatcher = StandardTestDispatcher()

        // Mock gameStorage.currentGame by default to emit null to avoid NPEs in some tests
        coEvery { mockGameStorage.currentGame } returns flowOf(null)


        repository = SudokuGameRepository(
            gameStorage = mockGameStorage,
            entryDao = mockEntryDao,
            ioDispatcher = testIoDispatcher,
            defaultDispatcher = testDefaultDispatcher
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `generateGrid calls generator with correct parameters and dispatcher`() = runTest(testDefaultDispatcher) {
        val difficulty = Difficulty.MEDIUM
        val expectedPuzzle = Array(9) { IntArray(9) { 1 } }
        val expectedSolution = Array(9) { IntArray(9) { 2 } }
        val expectedPair = Pair(expectedPuzzle, expectedSolution)

        coEvery { mockSudokuGenerator.generate(difficulty, testDefaultDispatcher) } returns expectedPair

        val result = repository.generateGrid(mockSudokuGenerator, difficulty)

        assertEquals(expectedPair, result)
        coVerify { mockSudokuGenerator.generate(difficulty, testDefaultDispatcher) }
    }

    @Test
    fun `hasGame returns true when game exists`() = runTest(testIoDispatcher) {
        val dummyGame = initializeEmptyGame()
        coEvery { mockGameStorage.currentGame } returns flowOf(dummyGame)

        val result = repository.hasGame()

        assertTrue(result)
    }

    @Test
    fun `hasGame returns false when no game exists`() = runTest(testIoDispatcher) {
        coEvery { mockGameStorage.currentGame } returns flowOf(null)

        val result = repository.hasGame()

        assertFalse(result)
    }

    @Test
    fun `hasGameFlow emits true when game exists`() = runTest {
        val dummyGame = initializeEmptyGame()
        coEvery { mockGameStorage.currentGame } returns flowOf(dummyGame)

        val result = repository.hasGameFlow().first()

        assertTrue(result)
    }

    @Test
    fun `hasGameFlow emits false when no game exists`() = runTest {
        coEvery { mockGameStorage.currentGame } returns flowOf(null)

        val result = repository.hasGameFlow().first()

        assertFalse(result)
    }

    @Test
    fun `saveGame calls gameStorage to save settings`() = runTest(testIoDispatcher) {
        val gameToSave = initializeEmptyGame()
        coEvery { mockGameStorage.saveThemeSettings(gameToSave) } coAnswers { } // Already relaxed

        repository.saveGame(gameToSave)

        coVerify { mockGameStorage.saveThemeSettings(gameToSave) }
    }

    @Test
    fun `loadGame returns game when it exists`() = runTest(testIoDispatcher) {
        val expectedGame = initializeEmptyGame()
        coEvery { mockGameStorage.currentGame } returns flowOf(expectedGame)

        val result = repository.loadGame()

        assertEquals(expectedGame, result)
    }

    @Test
    fun `loadGame returns null when no game exists`() = runTest(testIoDispatcher) {
        coEvery { mockGameStorage.currentGame } returns flowOf(null)

        val result = repository.loadGame()

        assertNull(result)
    }

    @Test
    fun `deleteGame calls gameStorage to clear game state`() = runTest(testIoDispatcher) {
        coEvery { mockGameStorage.clearGameState() } coAnswers { } // Already relaxed

        repository.deleteGame()

        coVerify { mockGameStorage.clearGameState() }
    }

    @Test
    fun `storeStatistic when game exists, inserts entry and deletes game`() = runTest(testIoDispatcher) {
        val gameState = initializeEmptyGame().copy(isSolved = true, duration = 5.seconds, mistakesMade = 2)
        coEvery { mockGameStorage.currentGame } returns flowOf(gameState) // for loadGame part
        coEvery { mockEntryDao.insertEntry(any()) } returns 1L // Assume successful insert

        repository.storeStatistic()

        val statisticEntrySlot = slot<StatisticEntry>()
        coVerify { mockEntryDao.insertEntry(capture(statisticEntrySlot)) }
        coVerify { mockGameStorage.clearGameState() } // for deleteGame part

        val capturedEntry = statisticEntrySlot.captured
        assertEquals(gameState.gameId, capturedEntry.gameId)
        assertEquals(gameState.difficulty.toString(), capturedEntry.difficulty)
        assertEquals(gameState.isSolved, capturedEntry.isSolved)
        assertEquals(gameState.duration.inWholeMilliseconds, capturedEntry.completionTimeMillis)
        assertEquals(gameState.mistakesMade, capturedEntry.mistakesMade)
        assertEquals(gameState.history.size, capturedEntry.stepsTaken)
        assertEquals(gameState.timerMillis, capturedEntry.timeStarted)
        assertTrue(capturedEntry.timeFinished > 0) // Basic check
    }

    @Test
    fun `storeStatistic when no game exists, does nothing`() = runTest(testIoDispatcher) {
        coEvery { mockGameStorage.currentGame } returns flowOf(null) // for loadGame part

        repository.storeStatistic()

        coVerify(exactly = 0) { mockEntryDao.insertEntry(any()) }
        coVerify(exactly = 0) { mockGameStorage.clearGameState() }
    }

    @Test
    fun `clearStatistic calls dao to clear statistics`() = runTest(testIoDispatcher) {
        coEvery { mockEntryDao.clearStatistic() } coAnswers { } // Already relaxed

        repository.clearStatistic()

        coVerify { mockEntryDao.clearStatistic() }
    }

    @Test
    fun `readStatistic returns empty stats when dao has no data`() = runTest(testIoDispatcher) {
        coEvery { mockEntryDao.getTotalGamesPlayed() } returns flowOf(0)
        coEvery { mockEntryDao.getTotalGamesWon() } returns flowOf(0)
        Difficulty.entries.forEach { diff ->
            val diffStr = diff.toString()
            coEvery { mockEntryDao.getGameStatisticsByDifficulty(diffStr) } returns flowOf(emptyList())
            coEvery { mockEntryDao.getSolvedGameStatisticsByDifficulty(diffStr) } returns flowOf(emptyList())
            coEvery { mockEntryDao.getAverageCompletionTimeMillisByDifficulty(diffStr) } returns flowOf(null)
            coEvery { mockEntryDao.getFastestCompletionTimeMillisByDifficulty(diffStr) } returns flowOf(null)
        }

        val stats = repository.readStatistic()

        assertEquals(0, stats.totalGamesPlayed)
        assertEquals(0, stats.totalGamesWon)
        stats.easyStats.also { diffStat ->
            assertEquals(0, diffStat.gamesPlayed)
            assertEquals(0, diffStat.gamesWon)
            assertEquals(0L, diffStat.averageCompletionTimeMillis)
            assertEquals(Long.MAX_VALUE, diffStat.fastestCompletionTimeMillis)
        }
        stats.mediumStats.also { diffStat ->
            assertEquals(0, diffStat.gamesPlayed)
            assertEquals(0, diffStat.gamesWon)
            assertEquals(0L, diffStat.averageCompletionTimeMillis)
            assertEquals(Long.MAX_VALUE, diffStat.fastestCompletionTimeMillis)
        }
        stats.hardStats.also { diffStat ->
            assertEquals(0, diffStat.gamesPlayed)
            assertEquals(0, diffStat.gamesWon)
            assertEquals(0L, diffStat.averageCompletionTimeMillis)
            assertEquals(Long.MAX_VALUE, diffStat.fastestCompletionTimeMillis)
        }
        stats.expertStats.also { diffStat ->
            assertEquals(0, diffStat.gamesPlayed)
            assertEquals(0, diffStat.gamesWon)
            assertEquals(0L, diffStat.averageCompletionTimeMillis)
            assertEquals(Long.MAX_VALUE, diffStat.fastestCompletionTimeMillis)
        }
    }

    @Test
    fun `readStatistic returns correct stats when dao has data`() = runTest(testIoDispatcher) {
        coEvery { mockEntryDao.getTotalGamesPlayed() } returns flowOf(10)
        coEvery { mockEntryDao.getTotalGamesWon() } returns flowOf(5)

        // Mock data for EASY difficulty
        val easyDiffStr = Difficulty.EASY.toString()
        val easyGameStatEntry = mockk<StatisticEntry>()
        val easySolvedGameStatEntry = mockk<StatisticEntry>()
        coEvery { mockEntryDao.getGameStatisticsByDifficulty(easyDiffStr) } returns flowOf(listOf(easyGameStatEntry, easyGameStatEntry)) // 2 played
        coEvery { mockEntryDao.getSolvedGameStatisticsByDifficulty(easyDiffStr) } returns flowOf(listOf(easySolvedGameStatEntry)) // 1 won
        coEvery { mockEntryDao.getAverageCompletionTimeMillisByDifficulty(easyDiffStr) } returns flowOf(120000L) // 2 mins
        coEvery { mockEntryDao.getFastestCompletionTimeMillisByDifficulty(easyDiffStr) } returns flowOf(90000L)  // 1.5 mins

        // Mock data for MEDIUM difficulty (no solved games)
        val mediumDiffStr = Difficulty.MEDIUM.toString()
        val mediumGameStatEntry = mockk<StatisticEntry>()
        coEvery { mockEntryDao.getGameStatisticsByDifficulty(mediumDiffStr) } returns flowOf(listOf(mediumGameStatEntry)) // 1 played
        coEvery { mockEntryDao.getSolvedGameStatisticsByDifficulty(mediumDiffStr) } returns flowOf(emptyList()) // 0 won
        coEvery { mockEntryDao.getAverageCompletionTimeMillisByDifficulty(mediumDiffStr) } returns flowOf(null)
        coEvery { mockEntryDao.getFastestCompletionTimeMillisByDifficulty(mediumDiffStr) } returns flowOf(null)

        // For HARD and EXPERT, assume no data (will use default empty from previous test's style)
        listOf(Difficulty.HARD, Difficulty.EXPERT).forEach { diff ->
            val diffStr = diff.toString()
            coEvery { mockEntryDao.getGameStatisticsByDifficulty(diffStr) } returns flowOf(emptyList())
            coEvery { mockEntryDao.getSolvedGameStatisticsByDifficulty(diffStr) } returns flowOf(emptyList())
            coEvery { mockEntryDao.getAverageCompletionTimeMillisByDifficulty(diffStr) } returns flowOf(null)
            coEvery { mockEntryDao.getFastestCompletionTimeMillisByDifficulty(diffStr) } returns flowOf(null)
        }


        val stats = repository.readStatistic()

        assertEquals(10, stats.totalGamesPlayed)
        assertEquals(5, stats.totalGamesWon)

        assertEquals(2, stats.easyStats.gamesPlayed)
        assertEquals(1, stats.easyStats.gamesWon)
        assertEquals(120000L, stats.easyStats.averageCompletionTimeMillis)
        assertEquals(90000L, stats.easyStats.fastestCompletionTimeMillis)

        assertEquals(1, stats.mediumStats.gamesPlayed)
        assertEquals(0, stats.mediumStats.gamesWon)
        assertEquals(0L, stats.mediumStats.averageCompletionTimeMillis) // Default for null
        assertEquals(Long.MAX_VALUE, stats.mediumStats.fastestCompletionTimeMillis) // Default for null

        assertEquals(0, stats.hardStats.gamesPlayed)
        assertEquals(Long.MAX_VALUE, stats.expertStats.fastestCompletionTimeMillis)
    }
}