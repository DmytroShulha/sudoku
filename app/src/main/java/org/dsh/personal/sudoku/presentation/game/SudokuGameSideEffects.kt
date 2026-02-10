package org.dsh.personal.sudoku.presentation.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.dsh.personal.sudoku.SudokuRoutes
import org.dsh.personal.sudoku.core.Navigator
import org.dsh.personal.sudoku.presentation.SudokuViewModel

@Composable
fun SudokuGameSideEffects(
    uiState: SudokuViewModel.SudokuUiState,
    navigator: Navigator,
    viewModel: SudokuViewModel
) {
    val gameState = uiState.game
    LaunchedEffect(gameState.isSolved) {
        if (gameState.isSolved) {
            navigator.goBackToRoot()
            navigator.navigate(SudokuRoutes.Success)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.handleIntent(SudokuViewModel.SudokuIntent.PauseGameTimer)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
