package org.dsh.personal.sudoku.presentation.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.presentation.SudokuViewModel
import org.dsh.personal.sudoku.presentation.capitalizeFirstLetter
import org.dsh.personal.sudoku.presentation.view.Dimens
import org.dsh.personal.sudoku.presentation.view.SudokuBoardView
import org.dsh.personal.sudoku.presentation.view.SudokuNumberInputRow
import org.dsh.personal.sudoku.presentation.view.SudokuNumberInputRowData

private const val WEIGHT04 = .4f
private const val WEIGHT06 = .6f

data class SudokuGameCallbacks(
    val onCellClick: (row: Int, col: Int) -> Unit,
    val onNumberClick: (Int) -> Unit,
    val undoClick: () -> Unit,
    val notesClick: () -> Unit,
    val resumeGame: () -> Unit,
)

@Composable
fun SudokuGame(
    modifier: Modifier = Modifier,
    gameState: SudokuGameState,
    sudokuSettings: SudokuViewModel.SudokuSettings,
    callbacks: SudokuGameCallbacks,
    windowSizeClass: WindowSizeClass,
) {
    val isWideDisplay = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded

    if (isWideDisplay) {
        TabletSudokuGame(modifier, sudokuSettings, callbacks, gameState)
    } else {
        PhoneSudokuGame(modifier, sudokuSettings, callbacks, gameState)
    }
}

@Composable
private fun PhoneSudokuGame(
    modifier: Modifier,
    sudokuSettings: SudokuViewModel.SudokuSettings,
    callbacks: SudokuGameCallbacks,
    gameState: SudokuGameState
) {
    Column(modifier = modifier) {
        if (sudokuSettings.isPaused) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Blurred board preview
                SudokuBoardView(
                    modifier = Modifier
                        .padding(top = Dimens.Medium, start = Dimens.Small, end = Dimens.Small)
                        .blur(16.dp),
                    board = gameState.boardState.grid,
                    selectedCellPosition = null,
                    onCellClick = { _, _ -> },
                    settings = sudokuSettings,
                )

                // Pause overlay
                PauseOverlay(
                    modifier = Modifier.align(Alignment.Center),
                    difficulty = gameState.difficulty.toString(),
                    duration = sudokuSettings.duration,
                    onResumeClick = callbacks.resumeGame
                )
            }
        } else {
            SudokuBoardView(
                modifier = Modifier.padding(
                    top = Dimens.Medium, start = Dimens.Small, end = Dimens.Small
                ),
                board = gameState.boardState.grid,
                selectedCellPosition = gameState.selectedCell,
                onCellClick = callbacks.onCellClick,
                settings = sudokuSettings,
            )
            Spacer(Modifier.height(Dimens.Medium))

            SudokuNumberInputRow(
                data = SudokuNumberInputRowData(
                    numbers = gameState.availableNumbers,
                    onNumberClick = callbacks.onNumberClick,
                    undoClick = callbacks.undoClick,
                    notesClick = callbacks.notesClick,
                    currentInputMode = gameState.inputMode,
                )
            )
        }
    }
}

@Composable
private fun TabletSudokuGame(
    modifier: Modifier,
    sudokuSettings: SudokuViewModel.SudokuSettings,
    callbacks: SudokuGameCallbacks,
    gameState: SudokuGameState
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Sudoku Board (takes more space)
        Box(
            modifier = Modifier
                .weight(WEIGHT06) // Take available space
                .aspectRatio(1f, true) // Keep square aspect ratio for the board
                .padding(Dimens.Large) // Add padding
        ) {
            if (sudokuSettings.isPaused) {
                // Blurred board preview
                SudokuBoardView(
                    board = gameState.boardState.grid,
                    selectedCellPosition = null,
                    onCellClick = { _, _ -> },
                    settings = sudokuSettings,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(16.dp)
                )

                // Pause overlay
                PauseOverlay(
                    modifier = Modifier.align(Alignment.Center),
                    difficulty = gameState.difficulty.toString(),
                    duration = sudokuSettings.duration,
                    onResumeClick = callbacks.resumeGame
                )
            } else {
                SudokuBoardView(
                    board = gameState.boardState.grid,
                    selectedCellPosition = gameState.selectedCell,
                    onCellClick = callbacks.onCellClick,
                    settings = sudokuSettings,
                    modifier = Modifier.fillMaxSize() // Fill the Box
                )
            }
        }

        // Number Input and Controls (aligned to the side)
        Column(
            modifier = Modifier
                .weight(WEIGHT04)
                .width(IntrinsicSize.Min) // Take minimum width
                .padding(Dimens.Large)
                .align(Alignment.CenterVertically) // Vertically center the column
        ) {
            if (!sudokuSettings.isPaused) {
                SudokuNumberInputRow(
                    data = SudokuNumberInputRowData(
                        numbers = gameState.availableNumbers,
                        onNumberClick = callbacks.onNumberClick,
                        undoClick = callbacks.undoClick,
                        notesClick = callbacks.notesClick,
                        currentInputMode = gameState.inputMode,
                    )
                )
                Spacer(Modifier.height(Dimens.Large))
                // Add other controls or information here for wider screens
            }
        }
    }
}

@Composable
private fun PauseOverlay(
    modifier: Modifier = Modifier,
    difficulty: String,
    duration: kotlin.time.Duration,
    onResumeClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(R.string.game_paused),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Difficulty info
                Text(
                    text = difficulty.capitalizeFirstLetter(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Time info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatDuration(duration),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Resume button
            Surface(
                onClick = onResumeClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        contentDescription = stringResource(R.string.play),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.tap_to_resume),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

private fun formatDuration(duration: kotlin.time.Duration): String {
    val minutes = duration.inWholeMinutes
    val seconds = duration.inWholeSeconds % 60
    return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
