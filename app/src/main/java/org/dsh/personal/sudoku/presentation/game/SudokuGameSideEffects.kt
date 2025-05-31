package org.dsh.personal.sudoku.presentation.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import org.dsh.personal.sudoku.SudokuRoutes
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.presentation.SudokuViewModel

@Composable
fun SudokuGameSideEffects(
    gameState: SudokuGameState,
    navController: NavController,
    viewModel: SudokuViewModel
) {
    LaunchedEffect(gameState.isSolved) {
        if (gameState.isSolved) {
            navController.popBackStack(
                SudokuRoutes.MAIN_MENU, false
            )
            navController.navigate(SudokuRoutes.SUCCESS_SCREEN)
        }
    }

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.pauseGameTimer()
                }

                else -> { /*Do nothing */
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
