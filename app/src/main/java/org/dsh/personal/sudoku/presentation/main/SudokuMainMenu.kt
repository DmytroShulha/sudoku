package org.dsh.personal.sudoku.presentation.main

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
import androidx.compose.material3.SheetState
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
import org.dsh.personal.sudoku.presentation.view.Dimens

private const val WeightSmall = .2f
private const val WeightMedium = .5f

data class SudokuMainMenuData(
    val onStartGame: (difficulty: Difficulty) -> Unit,
    val onAboutClick: () -> Unit,
    val onSettingsClick: () -> Unit,
    val onStatisticClick: () -> Unit,
    val hasContinueGame: Boolean,
    val onResumeGame: () -> Unit,
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuMainMenu(
    data: SudokuMainMenuData,
    modifier: Modifier = Modifier
) {
    var showNewGame by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    Surface {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(Dimens.Large)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(WeightMedium))
            Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null)
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.weight(WeightSmall))

            SudokuMainMenuItems(data) { showNewGame = true }
        }

        if (showNewGame) {
            ShowNewGame(
                sheetState = sheetState,
                data = data,
                onDismissBottom = { showNewGame = false })
        }

    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ShowNewGame(
    sheetState: SheetState,
    data: SudokuMainMenuData,
    onDismissBottom: ()->Unit,
) {
    val scope = rememberCoroutineScope()

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun onDifficultySelected(): (Difficulty) -> Unit = { selectedDifficulty ->
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissBottom()
                data.onStartGame(selectedDifficulty)
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun onDismiss(): () -> Unit = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissBottom()
            }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState, onDismissRequest = onDismissBottom) {
        DifficultySelectionSheet(
            difficulties = Difficulty.entries,
            onDifficultySelected = onDifficultySelected(),
            onDismiss = onDismiss()
        )
    }
}

@Composable
private fun SudokuMainMenuItems(
    data: SudokuMainMenuData,
    onNewGame: () -> Unit,
) {
    if (data.hasContinueGame) {
        MenuButton(
            text = stringResource(R.string.resume_game),
            icon = Icons.Filled.PlayArrow,
            onClick = data.onResumeGame,
            contentDescription = stringResource(R.string.resume_game_context_desc)
        )
        Spacer(modifier = Modifier.height(Dimens.Large))
    }

    MenuButton(
        text = stringResource(R.string.new_game),
        icon = Icons.Filled.AddCircleOutline,
        onClick = onNewGame,
        contentDescription = stringResource(R.string.new_game_context_desc)
    )
    Spacer(modifier = Modifier.height(16.dp))

    MenuButton(
        text = stringResource(R.string.settings),
        icon = Icons.Filled.Settings,
        onClick = data.onSettingsClick,
        contentDescription = stringResource(R.string.settings_content_desc)
    )
    Spacer(modifier = Modifier.height(Dimens.Large))

    MenuButton(
        text = stringResource(R.string.statistics),
        icon = Icons.Filled.BarChart,
        onClick = data.onStatisticClick,
        contentDescription = stringResource(R.string.statistics_content_desc)
    )
    Spacer(modifier = Modifier.height(Dimens.Large))

    MenuButton(
        text = stringResource(R.string.about),
        icon = Icons.Filled.Info,
        onClick = data.onAboutClick,
        contentDescription = stringResource(R.string.about_content_desc)
    )

    Spacer(modifier = Modifier.height(Dimens.Large))
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
        contentPadding = PaddingValues(horizontal = Dimens.Large, vertical = Dimens.Medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(Dimens.Icon),
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
                modifier = Modifier.size(Dimens.Icon)
            )
        }
    }
}
