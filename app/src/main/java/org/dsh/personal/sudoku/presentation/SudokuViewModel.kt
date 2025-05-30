package org.dsh.personal.sudoku.presentation

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.entity.InputMode
import org.dsh.personal.sudoku.domain.entity.SudokuBoardState
import org.dsh.personal.sudoku.domain.entity.SudokuCellState
import org.dsh.personal.sudoku.domain.entity.SudokuChange
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.entity.SudokuGameStatistic
import org.dsh.personal.sudoku.domain.useCase.CalculateAvailableNumbersUseCase
import org.dsh.personal.sudoku.domain.useCase.GenerateGameFieldUseCase
import org.dsh.personal.sudoku.domain.useCase.ValidateBoardUseCase
import org.dsh.personal.sudoku.presentation.game.ThemeSettingsManager
import org.dsh.personal.sudoku.domain.entity.SudokuBoardTheme
import org.dsh.personal.sudoku.domain.entity.SudokuCellNote
import org.dsh.personal.sudoku.domain.entity.SudokuEffects
import org.dsh.personal.sudoku.domain.useCase.CurrentGameHandler
import org.dsh.personal.sudoku.domain.useCase.ValidateNoteBoardUseCase
import org.dsh.personal.sudoku.utility.initializeEmptyGame
import org.dsh.personal.sudoku.utility.toBoard
import kotlin.collections.plus
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SudokuViewModel(
    private val themeSettingsManager: ThemeSettingsManager, // Inject ThemeSettingsManager
    private val generateGameField: GenerateGameFieldUseCase,
    private val calculateAvailableNumbers: CalculateAvailableNumbersUseCase,
    private val validateBoard: ValidateBoardUseCase,
    private val validateNoteBoard: ValidateNoteBoardUseCase,
    private val currentGameHandler: CurrentGameHandler,
) : ViewModel() {

    private val _gameState = MutableStateFlow(initializeEmptyGame())
    val gameState: StateFlow<SudokuGameState> = _gameState.asStateFlow()

    private val _sudokuSettings = MutableStateFlow(SudokuSettings())
    val sudokuSettings: StateFlow<SudokuSettings> = _sudokuSettings.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            themeSettingsManager.themeSettingsFlow.collect { themeSettings ->
                _sudokuSettings.update { it.copy(theme = themeSettings) }
            }
        }
        viewModelScope.launch {
            themeSettingsManager.effectsFlow.collect { effectsSettings ->
                _sudokuSettings.update { it.copy(effects = effectsSettings) }
            }
        }
        viewModelScope.launch {
            currentGameHandler.hasGameFlow().collect { hasGame->
                _sudokuSettings.update { it.copy(hasContinueGame = hasGame) }
            }
        }
    }

    fun updateAndSaveTheme(newTheme: SudokuBoardTheme) {
        viewModelScope.launch {
            themeSettingsManager.saveThemeSettings(newTheme)
        }
    }

    fun saveSettings(settings: SudokuSettings) {
        viewModelScope.launch {
            themeSettingsManager.saveThemeSettings(settings.theme)
            themeSettingsManager.saveEffectsSettings(settings.effects)
        }
    }

    private suspend fun initializeNewGame(difficulty: Difficulty): SudokuGameState {
        val generateGame = generateGameField(difficulty)
        val puzzleGridValues: List<List<Int>> = generateGame.second.toBoard()

        val initialGrid = puzzleGridValues.mapIndexed { rowIndex, row ->
            row.mapIndexed { colIndex, value ->
                SudokuCellState(
                    id = "r${rowIndex}_c${colIndex}",
                    value = value,
                    isClue = value != 0 // If value is not 0, it's a clue
                )
            }
                .toMutableStateList() // Using toMutableStateList for individual cell observability if needed
        }.toMutableStateList()


        val board = SudokuBoardState(
            grid = initialGrid,
            potentialSolution = generateGame.first.toBoard(),
            originalPuzzle = puzzleGridValues
        )

        startGameTimer()
        return SudokuGameState(
            boardState = board,
            difficulty = difficulty,
            gameId = System.currentTimeMillis().toString(),
            availableNumbers = calculateAvailableNumbers(puzzleGridValues),
            gameStatistic = SudokuGameStatistic(
                difficulty = difficulty,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
            )
        )
    }

    fun selectCell(row: Int, col: Int) {
        _gameState.update { currentState ->
            val currentGrid = currentState.boardState.grid
            val newGrid = currentGrid.map { r ->
                r.map { c -> c.copy(isHighlighted = false) }.toMutableStateList()
            }.toMutableStateList() // Deep copy and clear previous highlighting

            val selectedCell = currentState.boardState.getCell(row, col)
            var selectedNumberForInput: Int? = currentState.selectedNumberForInput

            selectedCell?.let { cell ->
                // If the selected cell has a value (is not empty), highlight other cells with the same value
                if (cell.value != 0) {
                    selectedNumberForInput =
                        cell.value // Set the number for input to the selected cell's value if not empty
                    for (r in newGrid.indices) {
                        for (c in newGrid[r].indices) {
                            newGrid[r][c].isHighlighted = newGrid[r][c].value == cell.value
                            newGrid[r][c].notes =
                                newGrid[r][c].notes.map { it.copy(isHighlighted = it.value == cell.value) }
                                    .toSet()
                        }
                    }
                } else {
                    selectedNumberForInput = null
                    for (r in newGrid.indices) {
                        for (c in newGrid[r].indices) {
                            newGrid[r][c].isHighlighted = false
                            newGrid[r][c].notes = newGrid[r][c].notes.map { it.copy(isHighlighted = false) }.toSet()
                        }
                    }
                }
            }

            val newBoardState = currentState.boardState.copy(grid = newGrid)

            currentState.copy(
                boardState = newBoardState,
                selectedCell = Pair(row, col),
                selectedNumberForInput = selectedNumberForInput
            )
        }
    }

    fun toggleInputMode() {
        _gameState.update { currentState ->
            currentState.copy(
                inputMode = when (currentState.inputMode) {
                    InputMode.VALUE -> InputMode.NOTES
                    InputMode.NOTES -> InputMode.VALUE
                }
            )
        }
    }

    fun undo() {
        viewModelScope.launch {
            _gameState.update { currentState ->
                if (currentState.history.isNotEmpty()) {
                    val lastChange = currentState.history.last()
                    val newHistory = currentState.history.dropLast(1) // Remove the last change

                    // Create a deep copy of the grid
                    val newGrid = currentState.boardState.grid.map { r ->
                        r.map { c -> c.copy() }.toMutableStateList()
                    }.toMutableStateList()

                    // Apply the old value back to the cell
                    val cellToUndo = newGrid[lastChange.rowIndex][lastChange.colIndex]
                    val updatedCell = cellToUndo.copy(
                        value = lastChange.oldValue,
                        isError = lastChange.oldIsError, // Revert to old error state
                        isHighlighted = lastChange.oldIsHighlighted // Revert to old highlighted state
                    )
                    newGrid[lastChange.rowIndex][lastChange.colIndex] = updatedCell

                    // Re-validate the board after undo
                    validateBoard(
                        grid = newGrid,
                        cellNumber = if (updatedCell.value != 0) updatedCell.value else cellToUndo.value,
                        cellRow = lastChange.rowIndex,
                        cellCol = lastChange.colIndex
                    )

                    // Update available numbers
                    val newAvailableNumbers =
                        calculateAvailableNumbers(newGrid.map { newRow-> newRow.map { it.value } })
                    // Add the undone change to the redo stack (optional)
                    val newRedoStack = currentState.redoStack + lastChange

                    val newBoardState = currentState.boardState.copy(grid = newGrid)

                    currentState.copy(
                        boardState = newBoardState,
                        history = newHistory,
                        redoStack = newRedoStack,
                        availableNumbers = newAvailableNumbers,
                        isSolved = checkWinCondition(newGrid) // Recheck win condition
                    ).also {
                        storeGameState(it)
                    }
                } else {
                    currentState // No history to undo
                }
            }
        }
    }

    private suspend fun storeGameState(state: SudokuGameState) {
        currentGameHandler.saveGame(state.copy(duration = sudokuSettings.value.duration))
    }

    fun inputNumber2(number: Int) {
        viewModelScope.launch {
            _gameState.update { currentState ->
                val selected = currentState.selectedCell
                if (selected != null) {
                    val (row, col) = selected
                    val currentCell = currentState.boardState.getCell(row, col)

                    if (currentCell != null && !currentCell.isClue) {
                        // Create a deep copy of the grid
                        val newGrid = currentState.boardState.grid.map { r ->
                            r.map { c -> c.copy() }.toMutableStateList()
                        }.toMutableStateList()
                        val cellToModify = newGrid[row][col]
                        // Create a SudokuChange object before modifying the cell
                        when(currentState.inputMode) {
                            InputMode.VALUE -> setValueToCell(currentState, row, col, number, cellToModify, newGrid)
                            InputMode.NOTES -> updateNotes(currentState, row, col, number, cellToModify, newGrid)
                        }.also {
                            storeGameState(it)
                        }

                    } else {
                        currentState // No change if no cell is selected or it's a clue
                    }
                } else {
                    currentState // No change if no cell is selected
                }
            }
        }
    }

    private suspend fun updateNotes(
        currentState: SudokuGameState,
        row: Int,
        col: Int,
        number: Int,
        cellToModify: SudokuCellState,
        newGrid: SnapshotStateList<SnapshotStateList<SudokuCellState>>
    ): SudokuGameState {
        val updatedCell = if(number != 0) {
            cellToModify.copy(
                notes = cellToModify.notes.toMutableSet()
                    .apply {
                        val exists = firstOrNull { it.value == number }
                        if (exists != null)
                            remove(exists)
                        else
                            add(SudokuCellNote(value = number))
                    }.toSet()
            )
        } else {
            cellToModify.copy(notes = emptySet())
        }
        newGrid[row][col] = updatedCell

        // Recalculate highlighting based on the new grid state
        val gridAfterHighlighting = newGrid.map { r ->
            r.map { c -> c.copy(isHighlighted = false) }
                .toMutableStateList()
        }.toMutableStateList() // Clear existing
        if (number != 0) {
            for (r in gridAfterHighlighting.indices) {
                for (c in gridAfterHighlighting[r].indices) {
                    if (gridAfterHighlighting[r][c].value == number) {
                        gridAfterHighlighting[r][c].isHighlighted = true
                    }
                }
            }
        } else {
            for (r in gridAfterHighlighting.indices) {
                for (c in gridAfterHighlighting[r].indices) {
                    gridAfterHighlighting[r][c].isHighlighted = false
                }
            }
        }

        validateNoteBoard(gridAfterHighlighting, number, row, col)

        val newBoardState =
            currentState.boardState.copy(grid = gridAfterHighlighting)

        return currentState.copy(boardState = newBoardState)
    }

    private suspend fun setValueToCell(
        currentState: SudokuGameState,
        row: Int,
        col: Int,
        number: Int,
        cellToModify: SudokuCellState,
        newGrid: SnapshotStateList<SnapshotStateList<SudokuCellState>>
    ): SudokuGameState {
        val change = SudokuChange(
            rowIndex = row,
            colIndex = col,
            oldValue = cellToModify.value,
            newValue = number, // The new value
            oldIsError = cellToModify.isError,
            newIsError = false, // Will be updated by validateBoard
            oldIsHighlighted = cellToModify.isHighlighted,
            newIsHighlighted = false // Will be updated by highlighting logic
        )

        val prevVal = newGrid[row][col].value
        // Update the cell value
        val updatedCell = newGrid[row][col].copy(value = number)
        newGrid[row][col] = updatedCell

        // Recalculate highlighting based on the new grid state
        val gridAfterHighlighting = newGrid.map { r ->
            r.map { c -> c.copy(isHighlighted = false) }
                .toMutableStateList()
        }.toMutableStateList() // Clear existing
        if (number != 0) {
            for (r in gridAfterHighlighting.indices) {
                for (c in gridAfterHighlighting[r].indices) {
                    if (gridAfterHighlighting[r][c].value == number) {
                        gridAfterHighlighting[r][c].isHighlighted = true
                    }
                }
            }
        } else {
            for (r in gridAfterHighlighting.indices) {
                for (c in gridAfterHighlighting[r].indices) {
                    gridAfterHighlighting[r][c].isHighlighted = false
                }
            }
        }

        // Validate the board after the change
        validateBoard(gridAfterHighlighting, if (number != 0) number else prevVal, row, col) // This will update isError

        val newBoardState =
            currentState.boardState.copy(grid = gridAfterHighlighting)
        val isNowSolved = checkWinCondition(gridAfterHighlighting)

        // Update history, clear redo stack
        val newHistory = currentState.history + change
        val newRedoStack = emptyList<SudokuChange>()

        // Update available numbers if you are tracking them based on placed numbers
        val newAvailableNumbers =
            calculateAvailableNumbers(newGrid.map { newRow-> newRow.map { value-> value.value } })

        return currentState.copy(
            boardState = newBoardState,
            isSolved = isNowSolved,
            history = newHistory,
            redoStack = newRedoStack,
            availableNumbers = newAvailableNumbers,
            // Update game statistic if game is solved
            gameStatistic = if (isNowSolved) {
                currentState.gameStatistic.copy(endTime = System.currentTimeMillis())
            } else {
                currentState.gameStatistic
            }
        )
    }

    fun startNewGame(difficulty: Difficulty) {
        viewModelScope.launch {
            _gameState.update { initializeNewGame(difficulty) }
        }
    }

    fun resumeGame() {
        viewModelScope.launch {
            currentGameHandler.loadGame()?.let { game->
                _gameState.update { game }
            }

            startGameTimer()
        }
    }

    private fun startGameTimer() {
        if (_sudokuSettings.value.timerState == TimerState.Stopped) {
            _sudokuSettings.update {
                it.copy(timerState = TimerState.Running, isPaused = false, duration = gameState.value.duration)
            }
            startCounting()
        } else {
            resumeGameTimer()
        }
    }

    fun pauseGameTimer() {
        if (_sudokuSettings.value.timerState == TimerState.Running) {
            _sudokuSettings.update {
                it.copy(isPaused = true, timerState = TimerState.Paused)
            }
            timerJob?.cancel() // Cancel the counting job
        }
    }

    // Call this when the user resumes the game
    fun resumeGameTimer() {
        if (_sudokuSettings.value.timerState == TimerState.Paused) {
            _sudokuSettings.update {
                it.copy(isPaused = false, timerState = TimerState.Running)
            }
            startCounting()
        }
    }

    private fun startCounting() {
        timerJob = viewModelScope.launch {
            while (isActive && _sudokuSettings.value.timerState == TimerState.Running) {
                delay(1.seconds)
                _sudokuSettings.value =
                    _sudokuSettings.value.copy(duration = _sudokuSettings.value.duration + 1.seconds)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel() // Ensure the job is cancelled when the ViewModel is cleared
    }


    // Placeholder for win condition check
    private fun checkWinCondition(grid: List<List<SudokuCellState>>): Boolean {
        // Check if all cells are filled and no cells have `isError = true`
        // And all Sudoku rules are satisfied.
        for (row in grid) {
            for (cell in row) {
                if (cell.isEmpty() || cell.isError) return false
            }
        }
        // Add comprehensive Sudoku rule check here too
        return true // Placeholder
    }

    data class SudokuSettings(
        val hasContinueGame: Boolean = false,
        val theme: SudokuBoardTheme = SudokuBoardTheme(),
        val effects: SudokuEffects = SudokuEffects(),
        val timerState: TimerState = TimerState.Stopped,
        val isPaused: Boolean = false,
        val duration: Duration = Duration.ZERO,
    )

    enum class TimerState {
        Running, Paused, Stopped
    }
}
