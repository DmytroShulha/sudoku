package org.dsh.personal.sudoku.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.twotone.Palette
import androidx.compose.material.icons.twotone.Pause
import androidx.compose.material.icons.twotone.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                text = stringResource(
                    R.string.level_is,
                    gameState.difficulty.toString().capitalizeFirstLetter()
                ),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }, actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPauseResumeClick) {
                    Icon(
                        imageVector = if (settings.isPaused) {
                            Icons.TwoTone.PlayArrow
                        } else {
                            Icons.TwoTone.Pause
                        }, contentDescription = stringResource(R.string.pause_game)
                    )
                }

                TimerChip(duration = settings.duration)

                IconButton(onClick = showThemeDialog) {
                    Icon(
                        imageVector = Icons.TwoTone.Palette,
                        contentDescription = stringResource(R.string.change_theme)
                    )
                }
            }
        }, navigationIcon = {
            IconButton(onClick = popBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun TimerChip(duration: Duration) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = duration.toFormat(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}



private fun Duration.toFormat(): String {
    val minutes = inWholeMinutes
    val seconds = inWholeSeconds % SecondsInMinute
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
