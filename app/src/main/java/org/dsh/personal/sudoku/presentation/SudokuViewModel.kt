package org.dsh.personal.sudoku.presentation

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
import org.dsh.personal.sudoku.domain.ProcessNoteData
import org.dsh.personal.sudoku.domain.SudokuHandler
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.entity.InputMode
import org.dsh.personal.sudoku.domain.entity.SudokuBoardTheme
import org.dsh.personal.sudoku.domain.entity.SudokuEffects
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.initializeNewGame
import org.dsh.personal.sudoku.domain.processNote
import org.dsh.personal.sudoku.presentation.game.ThemeSettingsManager
import org.dsh.personal.sudoku.utility.initializeEmptyGame
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SudokuViewModel(
    private val themeSettingsManager: ThemeSettingsManager,
    private val sudokuHandler: SudokuHandler,
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
            sudokuHandler.currentGameHandler.hasGameFlow().collect { hasGame->
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

    fun selectCell(row: Int, col: Int) {
        _gameState.update { currentState ->
            val currentGrid = currentState.boardState.grid
            val newGrid = currentGrid.map { r ->
                r.map { c ->
                    c.copy(
                        isHighlighted = false,
                        notes = c.notes.toMutableSet().map { it.copy(isHighlighted = false) }
                            .toSet()
                    )
                }.toMutableStateList()
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
                    sudokuHandler.validateBoard(
                        grid = newGrid,
                        cellNumber = if (updatedCell.value != 0) updatedCell.value else cellToUndo.value,
                        cellRow = lastChange.rowIndex,
                        cellCol = lastChange.colIndex
                    )

                    // Update available numbers
                    val newAvailableNumbers =
                        sudokuHandler.calculateAvailableNumbers(newGrid.map { newRow-> newRow.map { it.value } })
                    // Add the undone change to the redo stack (optional)
                    val newRedoStack = currentState.redoStack + lastChange

                    val newBoardState = currentState.boardState.copy(grid = newGrid)

                    currentState.copy(
                        boardState = newBoardState,
                        history = newHistory,
                        redoStack = newRedoStack,
                        availableNumbers = newAvailableNumbers,
                        isSolved = false
                    ).also {
                        sudokuHandler.storeGameState(state = it, duration = sudokuSettings.value.duration)
                    }
                } else {
                    currentState // No history to undo
                }
            }
        }
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
                        processNote(data = ProcessNoteData(
                            currentState = currentState,
                            row = row,
                            col = col,
                            number = number,
                            cellToModify = cellToModify,
                            newGrid = newGrid,
                            validateBoard = sudokuHandler.validateBoard::invoke,
                            calculateAvailableNumbers = sudokuHandler.calculateAvailableNumbers::invoke,
                            validateNoteBoard = sudokuHandler.validateNoteBoard::invoke,
                        ), defaultCoroutineDispatcher =  sudokuHandler.defaultCoroutineDispatcher).also {
                            sudokuHandler.storeGameState(it, sudokuSettings.value.duration)
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

    fun startNewGame(difficulty: Difficulty) {
        viewModelScope.launch {
            handleIntent(SudokuIntent.StartGameTimer)
            _gameState.update {
                initializeNewGame(
                    difficulty = difficulty,
                    generateGameField = sudokuHandler.generateGameField::invoke,
                    calculateAvailableNumbers = sudokuHandler.calculateAvailableNumbers::invoke
                )
            }
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
        timerJob?.cancel()
    }

    fun handleIntent(intent: SudokuIntent) {
        viewModelScope.launch {
            when (intent) {
                SudokuIntent.PauseGameTimer -> {
                    if (_sudokuSettings.value.timerState == TimerState.Running) {
                        _sudokuSettings.update {
                            it.copy(isPaused = true, timerState = TimerState.Paused)
                        }
                        timerJob?.cancel() // Cancel the counting job
                    }
                }
                SudokuIntent.ResumeGame -> {
                    sudokuHandler.currentGameHandler.loadGame()?.let { game ->
                        _gameState.update { game }
                    }
                    handleIntent(SudokuIntent.StartGameTimer)
                }

                SudokuIntent.ResumeGameTimer -> {
                    if (_sudokuSettings.value.timerState == TimerState.Paused) {
                        _sudokuSettings.update {
                            it.copy(isPaused = false, timerState = TimerState.Running)
                        }
                        startCounting()
                    }
                }
                SudokuIntent.StartGameTimer -> {
                    if (_sudokuSettings.value.timerState == TimerState.Stopped) {
                        _sudokuSettings.update {
                            it.copy(
                                timerState = TimerState.Running,
                                isPaused = false,
                                duration = gameState.value.duration
                            )
                        }
                        startCounting()
                    } else {
                        handleIntent(SudokuIntent.ResumeGameTimer)
                    }
                }
            }
        }
    }


    data class SudokuSettings(
        val hasContinueGame: Boolean = false,
        val theme: SudokuBoardTheme = SudokuBoardTheme(),
        val effects: SudokuEffects = SudokuEffects(),
        val timerState: TimerState = TimerState.Stopped,
        val isPaused: Boolean = false,
        val duration: Duration = Duration.ZERO,
    )


    sealed class SudokuIntent {
        data object ResumeGame: SudokuIntent()
        data object ResumeGameTimer: SudokuIntent()
        data object PauseGameTimer: SudokuIntent()
        data object StartGameTimer: SudokuIntent()
    }

    enum class TimerState {
        Running, Paused, Stopped
    }
}
