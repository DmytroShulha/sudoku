package org.dsh.personal.sudoku.domain

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.entity.SudokuBoardState
import org.dsh.personal.sudoku.domain.entity.SudokuGameStatistic
import org.dsh.personal.sudoku.domain.useCase.CalculateAvailableNumbersUseCase
import org.dsh.personal.sudoku.domain.useCase.CurrentGameHandler
import org.dsh.personal.sudoku.domain.useCase.GenerateGameFieldUseCase
import org.dsh.personal.sudoku.domain.useCase.ValidateBoardUseCase
import org.dsh.personal.sudoku.domain.useCase.ValidateNoteBoardUseCase
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class SudokuHandlerTest {

    private lateinit var mockGenerateGameFieldUseCase: GenerateGameFieldUseCase
    private lateinit var mockCalculateAvailableNumbersUseCase: CalculateAvailableNumbersUseCase
    private lateinit var mockValidateBoardUseCase: ValidateBoardUseCase
    private lateinit var mockValidateNoteBoardUseCase: ValidateNoteBoardUseCase
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockCurrentGameHandler: CurrentGameHandler

    private lateinit var sudokuHandler: SudokuHandler

    @Before
    fun setUp() {
        mockGenerateGameFieldUseCase = mockk()
        mockCalculateAvailableNumbersUseCase = mockk()
        mockValidateBoardUseCase = mockk()
        mockValidateNoteBoardUseCase = mockk()
        mockCurrentGameHandler = mockk(relaxUnitFun = true)

        sudokuHandler = SudokuHandler(
            generateGameField = mockGenerateGameFieldUseCase,
            calculateAvailableNumbers = mockCalculateAvailableNumbersUseCase,
            validateBoard = mockValidateBoardUseCase,
            validateNoteBoard = mockValidateNoteBoardUseCase,
            defaultCoroutineDispatcher = testDispatcher,
            currentGameHandler = mockCurrentGameHandler
        )
    }

    // Updated to align with new SudokuCellState and SudokuGameState
    private fun createDummyGameState(duration: Duration = Duration.ZERO): SudokuGameState {
        // Mocking SudokuBoardState and SudokuGameStatistic as their internal structure
        // is not the focus of SudokuHandler.storeGameState test.
        val mockBoardState: SudokuBoardState = mockk(relaxed = true)
        val mockGameStatistic: SudokuGameStatistic = mockk(relaxed = true)

        return SudokuGameState(
            boardState = mockBoardState,
            gameStatistic = mockGameStatistic,
            difficulty = Difficulty.EASY,
            duration = duration
            // Other fields will use their default values from SudokuGameState constructor
        )
    }

    @Test
    fun `storeGameState calls currentGameHandler with updated duration`() =
        runTest(testDispatcher) {
            // Arrange
            val initialDuration = 10.seconds
            val newDuration = 120.seconds
            val initialGameState = createDummyGameState(duration = initialDuration)
            // .copy should still work as duration is a top-level field
            val expectedGameStateAfterStore = initialGameState.copy(duration = newDuration)

            // Act
            sudokuHandler.storeGameState(initialGameState, newDuration)

            // Assert
            coVerify(exactly = 1) { mockCurrentGameHandler.saveGame(expectedGameStateAfterStore) }
        }

    @Test
    fun `storeGameState with different state and duration`() = runTest(testDispatcher) {
        // Arrange
        val initialDuration = 0.seconds
        val newDuration = 30.seconds

        // For SudokuBoardState, we'll use a mock.
        // If specific board content were needed for SudokuHandler logic (it's not for storeGameState),
        // this mock would need more specific setup.
        val mockSpecificBoardState: SudokuBoardState = mockk(relaxed = true)
        // Similarly for gameStatistic
        val mockSpecificGameStatistic: SudokuGameStatistic = mockk(relaxed = true)
        // Example of how you might set a field if the mock wasn't relaxed and a getter was called:
        // every { mockSpecificGameStatistic.mistakes } returns 2

        val initialGameState = SudokuGameState(
            boardState = mockSpecificBoardState,
            gameStatistic = mockSpecificGameStatistic,
            difficulty = Difficulty.HARD,
            duration = initialDuration,
            mistakesMade = 2 // This field is directly in SudokuGameState
            // gameId can be defaulted or set if needed: gameId = "test-id"
        )
        val expectedGameStateAfterStore = initialGameState.copy(duration = newDuration)

        // Act
        sudokuHandler.storeGameState(initialGameState, newDuration)

        // Assert
        coVerify(exactly = 1) { mockCurrentGameHandler.saveGame(expectedGameStateAfterStore) }
    }
}