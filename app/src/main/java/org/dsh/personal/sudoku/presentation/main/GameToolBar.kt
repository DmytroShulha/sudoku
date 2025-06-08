package org.dsh.personal.sudoku.presentation.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.twotone.Palette
import androidx.compose.material.icons.twotone.Pause
import androidx.compose.material.icons.twotone.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.presentation.SudokuViewModel
import org.dsh.personal.sudoku.presentation.capitalizeFirstLetter
import java.util.Locale
import kotlin.time.Duration

private const val SecondsInMinute = 60

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GameToolBar(
    gameState: SudokuGameState,
    settings: SudokuViewModel.SudokuSettings,
    showThemeDialog: () -> Unit,
    onPauseResumeClick: () -> Unit,
    popBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                stringResource(
                    R.string.level_is,
                    gameState.difficulty.toString().capitalizeFirstLetter()
                )
            )
        }, actions = {
            IconButton(onClick = onPauseResumeClick ) {
                Icon(
                    imageVector = if (settings.isPaused) {
                        Icons.TwoTone.PlayArrow
                    } else {
                        Icons.TwoTone.Pause
                    }, contentDescription = stringResource(R.string.pause_game)
                )
            }

            Text(settings.duration.toFormat())

            IconButton(onClick = showThemeDialog) {
                Icon(
                    imageVector = Icons.TwoTone.Palette,
                    contentDescription = stringResource(R.string.change_theme)
                )
            }
        }, navigationIcon = {
            IconButton(onClick = popBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}



private fun Duration.toFormat(): String {
    val minutes = inWholeMinutes
    val seconds = inWholeSeconds % SecondsInMinute
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
