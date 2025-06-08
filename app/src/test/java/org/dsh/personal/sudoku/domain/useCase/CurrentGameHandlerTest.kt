package org.dsh.personal.sudoku.domain.useCase


import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.repository.SudokuRepository
import org.dsh.personal.sudoku.utility.initializeEmptyGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class CurrentGameHandlerTest {

    private lateinit var mockRepository: SudokuRepository
    private lateinit var currentGameHandler: CurrentGameHandler

    // Helper to create a dummy game state
    private fun createDummyGameState(): SudokuGameState {
        return initializeEmptyGame() // Assuming you have this utility from other tests
    }

    @Before
    fun setUp() {
        mockRepository = mockk(relaxUnitFun = true) // relaxUnitFun for saveGame
        currentGameHandler = CurrentGameHandler(mockRepository)
    }

    @Test
    fun `hasGame when repository returns true`() = runTest {
        coEvery { mockRepository.hasGame() } returns true

        val result = currentGameHandler.hasGame()

        assertTrue(result)
        coVerify(exactly = 1) { mockRepository.hasGame() }
    }

    @Test
    fun `hasGame when repository returns false`() = runTest {
        coEvery { mockRepository.hasGame() } returns false

        val result = currentGameHandler.hasGame()

        assertFalse(result)
        coVerify(exactly = 1) { mockRepository.hasGame() }
    }

    @Test
    fun `hasGameFlow returns flow from repository`() = runTest {
        val expectedFlow = flowOf(true)
        every { mockRepository.hasGameFlow() } returns expectedFlow

        val resultFlow = currentGameHandler.hasGameFlow()
        val firstEmission = resultFlow.first()

        assertEquals(expectedFlow, resultFlow)
        assertTrue(firstEmission)
        verify(exactly = 1) { mockRepository.hasGameFlow() }
    }

    @Test
    fun `hasGameFlow emits updates from repository flow`() = runTest {
        val flowValues = listOf(false, true, false)
        every { mockRepository.hasGameFlow() } returns flowOf(*flowValues.toTypedArray())

        val resultFlow = currentGameHandler.hasGameFlow()
        val collectedValues = mutableListOf<Boolean>()
        resultFlow.collect { collectedValues.add(it) }


        assertEquals(flowValues, collectedValues)
        verify(exactly = 1) { mockRepository.hasGameFlow() }
    }

    @Test
    fun `saveGame calls repository saveGame`() = runTest {
        val dummyGame = createDummyGameState()
        // coEvery { mockRepository.saveGame(dummyGame) } returns Unit // Covered by relaxUnitFun

        currentGameHandler.saveGame(dummyGame)

        coVerify(exactly = 1) { mockRepository.saveGame(dummyGame) }
    }

    @Test
    fun `loadGame when repository returns a game`() = runTest {
        val expectedGame = createDummyGameState()
        coEvery { mockRepository.loadGame() } returns expectedGame

        val result = currentGameHandler.loadGame()

        assertNotNull(result)
        assertEquals(expectedGame, result)
        coVerify(exactly = 1) { mockRepository.loadGame() }
    }

    @Test
    fun `loadGame when repository returns null`() = runTest {
        coEvery { mockRepository.loadGame() } returns null

        val result = currentGameHandler.loadGame()

        assertNull(result)
        coVerify(exactly = 1) { mockRepository.loadGame() }
    }
}