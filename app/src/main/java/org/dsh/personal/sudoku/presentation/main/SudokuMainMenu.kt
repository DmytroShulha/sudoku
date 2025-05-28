package org.dsh.personal.sudoku.presentation.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.R

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SudokuMainMenu(
    onStartGame: (difficulty: Difficulty) -> Unit,
    onAboutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatisticClick: () -> Unit,
    hasContinueGame: Boolean,
    onResumeGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewGame by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    Surface {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(.5f))
            Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null)
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            Spacer(Modifier.weight(.2f))

            if (hasContinueGame) {
                MenuButton(
                    text = stringResource(R.string.resume_game),
                    icon = Icons.Filled.PlayArrow,
                    onClick = onResumeGame,
                    contentDescription = stringResource(R.string.resume_game_context_desc)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            MenuButton(
                text = stringResource(R.string.new_game),
                icon = Icons.Filled.AddCircleOutline,
                onClick = { showNewGame = true },
                contentDescription = stringResource(R.string.new_game_context_desc)
            )
            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                text = stringResource(R.string.settings),
                icon = Icons.Filled.Settings,
                onClick = onSettingsClick,
                contentDescription = stringResource(R.string.settings_content_desc)
            )
            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                text = stringResource(R.string.statistics),
                icon = Icons.Filled.BarChart,
                onClick = onStatisticClick,
                contentDescription = stringResource(R.string.statistics_content_desc)
            )
            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                text = stringResource(R.string.about),
                icon = Icons.Filled.Info,
                onClick = onAboutClick,
                contentDescription = stringResource(R.string.about_content_desc)
            )
            Spacer(Modifier.weight(.2f))
        }

        if (showNewGame) {
            ModalBottomSheet(
                sheetState = sheetState, onDismissRequest = { showNewGame = false }) {
                DifficultySelectionSheet(
                    difficulties = Difficulty.entries,
                    onDifficultySelected = { selectedDifficulty ->
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showNewGame = false
                                onStartGame(selectedDifficulty)
                            }
                        }
                    },
                    onDismiss = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showNewGame = false
                            }
                        }
                    })
            }
        }

    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 18.sp,
            )

            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}