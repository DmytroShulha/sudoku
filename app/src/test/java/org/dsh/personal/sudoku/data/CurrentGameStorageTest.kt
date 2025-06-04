package org.dsh.personal.sudoku.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.utility.initializeEmptyGame
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@ExperimentalCoroutinesApi
class CurrentGameStorageTest {
    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder.builder()
        .assureDeletion()
        .build()

    val dataStore by lazy {
        PreferenceDataStoreFactory.create(
            produceFile = { temporaryFolder.newFile("test_store.preferences_pb") },
        )
    }
    private val storage = CurrentGameStorage(dataStore)


    @Test
    fun `saveAndLoadGameState should retrieve the saved game state`() = runTest {
        val originalGameState = initializeEmptyGame().copy(difficulty = Difficulty.MEDIUM)
        storage.saveThemeSettings(originalGameState)

        val loadedGameState = storage.currentGame.first()

        Assert.assertNotNull("Loaded game state should not be null", loadedGameState)
        // Ensure SudokuGameState has a proper equals implementation, especially for arrays
        Assert.assertEquals("Loaded game state should match the original", originalGameState, loadedGameState)
    }

    @Test
    fun `clearGameState should remove the saved game state`() = runTest {
        val gameStateToSaveAndClear = initializeEmptyGame().copy(gameId = "gameToClear")
        storage.saveThemeSettings(gameStateToSaveAndClear)

        // Verify it was saved
        Assert.assertNotNull("Game state should be saved before clearing", storage.currentGame.first())

        storage.clearGameState()
        val loadedGameStateAfterClear = storage.currentGame.first()

        Assert.assertNull("Loaded game state should be null after clearing", loadedGameStateAfterClear)
    }

    @Test
    fun `loadGameState when none saved should return null`() = runTest {
        val loadedGameState = storage.currentGame.first()
        Assert.assertNull("Loaded game state should be null when none has been saved", loadedGameState)
    }

    @Test
    fun `saveGameState should overwrite an existing game state`() = runTest {
        val firstGameState = initializeEmptyGame().copy(gameId = "firstGame", difficulty = Difficulty.EASY, isSolved = true)
        storage.saveThemeSettings(firstGameState)

        val loadedFirstState = storage.currentGame.first()
        Assert.assertEquals("First saved state should be retrievable", firstGameState, loadedFirstState)

        val secondGameState = initializeEmptyGame().copy(gameId = "secondGame", difficulty = Difficulty.HARD, isSolved = false)
        storage.saveThemeSettings(secondGameState)

        val loadedSecondState = storage.currentGame.first()
        Assert.assertNotNull("Second game state should not be null after saving over", loadedSecondState)
        Assert.assertEquals("Loaded game state should be the second (overwritten) state", secondGameState, loadedSecondState)
    }

    @Test
    fun `currentGame flow emits null initially if no game is saved`() = runTest {
        val initialEmission = storage.currentGame.first()
        Assert.assertNull("Initial emission should be null if no game state is saved", initialEmission)
    }

    @Test
    fun `currentGame flow emits new state after saveThemeSettings`() = runTest {
        val gameState = initializeEmptyGame().copy(gameId = "flowTest", difficulty = Difficulty.EXPERT)

        // Start collecting the flow (optional for this specific test structure with .first(),
        // but useful if you were to test multiple emissions)

        storage.saveThemeSettings(gameState)
        val emittedState = storage.currentGame.first() // Gets the latest value

        Assert.assertEquals("Flow should emit the newly saved game state", gameState, emittedState)
    }

    @Test
    fun `currentGame flow emits null after clearGameState`() = runTest {
        val gameState = initializeEmptyGame().copy(gameId = "flowClearTest")
        storage.saveThemeSettings(gameState)
        Assert.assertNotNull("Should be saved initially", storage.currentGame.first())

        storage.clearGameState()
        val emittedStateAfterClear = storage.currentGame.first()

        Assert.assertNull("Flow should emit null after game state is cleared", emittedStateAfterClear)
    }



}