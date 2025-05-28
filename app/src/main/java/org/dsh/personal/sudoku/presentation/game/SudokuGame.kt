package org.dsh.personal.sudoku.presentation.game

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.presentation.SudokuViewModel
import org.dsh.personal.sudoku.presentation.view.SudokuBoardView
import org.dsh.personal.sudoku.presentation.view.SudokuNumberInputRow

@Composable
fun SudokuGame(
    modifier: Modifier = Modifier,
    gameState: SudokuGameState,
    onCellClick: (row: Int, col: Int) -> Unit,
    onNumberClick: (Int) -> Unit,
    undoClick: () -> Unit,
    notesClick: () -> Unit,
    sudokuSettings: SudokuViewModel.SudokuSettings,
    resumeGame: () -> Unit,
    windowSizeClass: WindowSizeClass,
) {

    val isWideDisplay = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded

    if (isWideDisplay) {
        // Layout for wider screens (e.g., tablet landscape)
        Row(modifier = modifier.fillMaxSize()) {
            // Sudoku Board (takes more space)
            Box(
                modifier = Modifier
                    .weight(0.6f) // Take available space
                    .aspectRatio(1f, true) // Keep square aspect ratio for the board
                    .padding(16.dp) // Add padding
            ) {
                if (sudokuSettings.isPaused) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        IconButton(
                            onClick = { resumeGame() },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = stringResource(R.string.play), modifier = Modifier.size(72.dp))
                        }
                    }
                } else {
                    SudokuBoardView(
                        board = gameState.boardState.grid,
                        selectedCellPosition = gameState.selectedCell,
                        onCellClick = onCellClick,
                        settings = sudokuSettings,
                        modifier = Modifier.fillMaxSize() // Fill the Box
                    )
                }
            }

            // Number Input and Controls (aligned to the side)
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .width(IntrinsicSize.Min) // Take minimum width
                    .padding(horizontal = 16.dp, vertical = 16.dp) // Add padding
                    .align(Alignment.CenterVertically) // Vertically center the column
            ) {
                if (!sudokuSettings.isPaused) {
                    SudokuNumberInputRow(
                        numbers = gameState.availableNumbers,
                        onNumberClick = onNumberClick,
                        undoClick = undoClick,
                        notesClick = notesClick,
                        currentInputMode = gameState.inputMode,
                    )
                    Spacer(Modifier.height(16.dp))
                    // Add other controls or information here for wider screens
                }
            }
        }
    } else {
        // Original layout for smaller screens (phones)
        Column(modifier = modifier) {
            Spacer(Modifier.height(132.dp)) // Consider adjusting this spacer for phones

            if (sudokuSettings.isPaused) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    IconButton(
                        onClick = { resumeGame() },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = stringResource(R.string.play), modifier = Modifier.size(72.dp))
                    }
                }
            } else {
                SudokuBoardView(
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
                    board = gameState.boardState.grid,
                    selectedCellPosition = gameState.selectedCell,
                    onCellClick = onCellClick,
                    settings = sudokuSettings,
                )
                Spacer(Modifier.height(8.dp))

                SudokuNumberInputRow(
                    numbers = gameState.availableNumbers,
                    onNumberClick = onNumberClick,
                    undoClick = undoClick,
                    notesClick = notesClick,
                    currentInputMode = gameState.inputMode,
                )
            }
        }
    }
}
