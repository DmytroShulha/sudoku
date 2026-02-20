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
import org.dsh.personal.sudoku.presentation.SudokuViewModel
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
    uiState: SudokuViewModel.SudokuUiState,
    callbacks: SudokuGameCallbacks,
    windowSizeClass: WindowSizeClass,
) {
    val isWideDisplay = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded

    if (isWideDisplay) {
        TabletSudokuGame(modifier, uiState, callbacks)
    } else {
        PhoneSudokuGame(modifier, uiState, callbacks)
    }
}

@Composable
private fun PhoneSudokuGame(
    modifier: Modifier,
    uiState: SudokuViewModel.SudokuUiState,
    callbacks: SudokuGameCallbacks
) {
    val gameState = uiState.game
    Column(modifier = modifier) {
        if (uiState.isPaused) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(
                    onClick = callbacks.resumeGame, modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        contentDescription = stringResource(R.string.play),
                        modifier = Modifier.size(72.dp)
                    )
                }
            }
        } else {
            SudokuBoardView(
                modifier = Modifier.padding(
                    top = Dimens.Medium, start = Dimens.Small, end = Dimens.Small
                ),
                selectedCellPosition = gameState.selectedCell,
                onCellClick = callbacks.onCellClick,
                uiState = uiState,
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
    uiState: SudokuViewModel.SudokuUiState,
    callbacks: SudokuGameCallbacks
) {
    val gameState = uiState.game
    Row(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(WEIGHT06)
                .aspectRatio(1f, true)
                .padding(Dimens.Large)
        ) {
            if (uiState.isPaused) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    IconButton(
                        onClick = callbacks.resumeGame, modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(
                            Icons.Outlined.PlayArrow,
                            contentDescription = stringResource(R.string.play),
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }
            } else {
                SudokuBoardView(
                    selectedCellPosition = gameState.selectedCell,
                    onCellClick = callbacks.onCellClick,
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(WEIGHT04)
                .width(IntrinsicSize.Min)
                .padding(Dimens.Large)
                .align(Alignment.CenterVertically)
        ) {
            if (!uiState.isPaused) {
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
            }
        }
    }
}
