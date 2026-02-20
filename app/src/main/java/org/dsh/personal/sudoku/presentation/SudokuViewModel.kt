package org.dsh.personal.sudoku.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
import kotlin.time.Duration.Companion.seconds

class SudokuViewModel(
    private val themeSettingsManager: ThemeSettingsManager,
    private val sudokuHandler: SudokuHandler,
) : ViewModel() {

    private val _gameState = MutableStateFlow(initializeEmptyGame())
    private val _timerState = MutableStateFlow(TimerState.Stopped)
    private val _isPaused = MutableStateFlow(false)

    val uiState: StateFlow<SudokuUiState> = combine(
        combine(_gameState, _timerState, _isPaused) { game, timer, paused -> 
            Triple(game, timer, paused) 
        },
        themeSettingsManager.themeSettingsFlow,
        themeSettingsManager.effectsFlow,
        sudokuHandler.currentGameHandler.hasGameFlow()
    ) { (game, timer, paused), theme, effects, hasContinueGame ->
        SudokuUiState(
            game = game,
            timerState = timer,
            isPaused = paused,
            theme = theme,
            effects = effects,
            hasContinueGame = hasContinueGame
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SudokuUiState(game = _gameState.value)
    )

    private var timerJob: Job? = null

    fun handleIntent(intent: SudokuIntent) {
        when (intent) {
            is SudokuIntent.SelectCell -> selectCell(intent.row, intent.col)
            is SudokuIntent.InputNumber -> inputNumber(intent.number)
            is SudokuIntent.ToggleInputMode -> toggleInputMode()
            SudokuIntent.Undo -> undo()
            is SudokuIntent.StartNewGame -> startNewGame(intent.difficulty)
            SudokuIntent.ResumeGame -> resumeGame()
            SudokuIntent.ResumeGameTimer -> resumeTimer()
            SudokuIntent.PauseGameTimer -> pauseTimer()
            SudokuIntent.StartGameTimer -> startTimer()
            is SudokuIntent.UpdateTheme -> updateAndSaveTheme(intent.theme)
            is SudokuIntent.SaveSettings -> saveSettings(intent.theme, intent.effects)
        }
    }

    private fun updateAndSaveTheme(newTheme: SudokuBoardTheme) {
        viewModelScope.launch {
            themeSettingsManager.saveThemeSettings(newTheme)
        }
    }

    private fun saveSettings(theme: SudokuBoardTheme, effects: SudokuEffects) {
        viewModelScope.launch {
            themeSettingsManager.saveThemeSettings(theme)
            themeSettingsManager.saveEffectsSettings(effects)
        }
    }

    private fun selectCell(row: Int, col: Int) {
        _gameState.update { currentState ->
            val selectedCell = currentState.boardState.getCell(row, col)
            val selectedValue = selectedCell?.value ?: 0
            
            val newGrid = currentState.boardState.grid.map { r ->
                r.map { c ->
                    val isHighlighted = selectedValue != 0 && c.value == selectedValue
                    c.copy(
                        isHighlighted = isHighlighted,
                        notes = c.notes.map { it.copy(isHighlighted = selectedValue != 0 && it.value == selectedValue) }.toSet()
                    )
                }
            }

            currentState.copy(
                boardState = currentState.boardState.copy(grid = newGrid),
                selectedCell = Pair(row, col),
                selectedNumberForInput = if (selectedValue != 0) selectedValue else null
            )
        }
    }

    private fun toggleInputMode() {
        _gameState.update { currentState ->
            currentState.copy(
                inputMode = if (currentState.inputMode == InputMode.VALUE) InputMode.NOTES else InputMode.VALUE
            )
        }
    }

    private fun undo() {
        viewModelScope.launch {
            _gameState.update { currentState ->
                if (currentState.history.isNotEmpty()) {
                    val lastChange = currentState.history.last()
                    val newHistory = currentState.history.dropLast(1)

                    val newGrid = currentState.boardState.grid.map { r ->
                        r.map { c -> c.copy() }.toMutableList()
                    }.toMutableList()

                    val cellToUndo = newGrid[lastChange.rowIndex][lastChange.colIndex]
                    val updatedCell = cellToUndo.copy(
                        value = lastChange.oldValue,
                        isError = lastChange.oldIsError,
                        isHighlighted = lastChange.oldIsHighlighted
                    )
                    newGrid[lastChange.rowIndex][lastChange.colIndex] = updatedCell

                    sudokuHandler.validateBoard(
                        grid = newGrid,
                        cellNumber = if (updatedCell.value != 0) updatedCell.value else cellToUndo.value,
                        cellRow = lastChange.rowIndex,
                        cellCol = lastChange.colIndex
                    )

                    val newAvailableNumbers = sudokuHandler.calculateAvailableNumbers(newGrid.map { row -> row.map { it.value } })
                    
                    currentState.copy(
                        boardState = currentState.boardState.copy(grid = newGrid.map { it.toList() }),
                        history = newHistory,
                        redoStack = currentState.redoStack + lastChange,
                        availableNumbers = newAvailableNumbers,
                        isSolved = false
                    ).also {
                        sudokuHandler.storeGameState(state = it, duration = it.duration)
                    }
                } else currentState
            }
        }
    }

    private fun inputNumber(number: Int) {
        viewModelScope.launch {
            _gameState.update { currentState ->
                val selected = currentState.selectedCell ?: return@update currentState
                val (row, col) = selected
                val currentCell = currentState.boardState.getCell(row, col)

                if (currentCell != null && !currentCell.isClue) {
                    val newGrid = currentState.boardState.grid.map { r ->
                        r.map { c -> c.copy() }.toMutableList()
                    }.toMutableList()
                    
                    val cellToModify = newGrid[row][col]
                    
                    processNote(
                        data = ProcessNoteData(
                            currentState = currentState,
                            row = row,
                            col = col,
                            number = number,
                            cellToModify = cellToModify,
                            newGrid = newGrid,
                            validateBoard = sudokuHandler.validateBoard::invoke,
                            calculateAvailableNumbers = sudokuHandler.calculateAvailableNumbers::invoke,
                            validateNoteBoard = sudokuHandler.validateNoteBoard::invoke,
                        ),
                        defaultCoroutineDispatcher = sudokuHandler.defaultCoroutineDispatcher
                    ).also {
                        sudokuHandler.storeGameState(it, it.duration)
                    }
                } else currentState
            }
        }
    }

    private fun startNewGame(difficulty: Difficulty) {
        viewModelScope.launch {
            _gameState.value = initializeNewGame(
                difficulty = difficulty,
                generateGameField = sudokuHandler.generateGameField::invoke,
                calculateAvailableNumbers = sudokuHandler.calculateAvailableNumbers::invoke
            )
            handleIntent(SudokuIntent.StartGameTimer)
        }
    }

    private fun resumeGame() {
        viewModelScope.launch {
            sudokuHandler.currentGameHandler.loadGame()?.let { game ->
                _gameState.value = game
                handleIntent(SudokuIntent.StartGameTimer)
            }
        }
    }

    private fun startTimer() {
        if (_timerState.value == TimerState.Stopped) {
            _timerState.value = TimerState.Running
            _isPaused.value = false
            startCounting()
        } else {
            resumeTimer()
        }
    }

    private fun pauseTimer() {
        if (_timerState.value == TimerState.Running) {
            _isPaused.value = true
            _timerState.value = TimerState.Paused
            timerJob?.cancel()
        }
    }

    private fun resumeTimer() {
        if (_timerState.value == TimerState.Paused) {
            _isPaused.value = false
            _timerState.value = TimerState.Running
            startCounting()
        }
    }

    private fun startCounting() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _timerState.value == TimerState.Running) {
                delay(1.seconds)
                _gameState.update { it.copy(duration = it.duration + 1.seconds) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    data class SudokuUiState(
        val game: SudokuGameState,
        val hasContinueGame: Boolean = false,
        val theme: SudokuBoardTheme = SudokuBoardTheme(),
        val effects: SudokuEffects = SudokuEffects(),
        val timerState: TimerState = TimerState.Stopped,
        val isPaused: Boolean = false,
    )

    sealed class SudokuIntent {
        data class SelectCell(val row: Int, val col: Int) : SudokuIntent()
        data class InputNumber(val number: Int) : SudokuIntent()
        data object ToggleInputMode : SudokuIntent()
        data object Undo : SudokuIntent()
        data class StartNewGame(val difficulty: Difficulty) : SudokuIntent()
        data object ResumeGame : SudokuIntent()
        data object ResumeGameTimer : SudokuIntent()
        data object PauseGameTimer : SudokuIntent()
        data object StartGameTimer : SudokuIntent()
        data class UpdateTheme(val theme: SudokuBoardTheme) : SudokuIntent()
        data class SaveSettings(val theme: SudokuBoardTheme, val effects: SudokuEffects) : SudokuIntent()
    }

    enum class TimerState {
        Running, Paused, Stopped
    }
}
