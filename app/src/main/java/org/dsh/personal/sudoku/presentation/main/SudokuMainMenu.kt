package org.dsh.personal.sudoku.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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

private const val HeaderSpacerWeight = 0.5f
private const val ContentSpacerWeight = 0.2f

data class SudokuMainMenuData(
    val onStartGame: (difficulty: Difficulty) -> Unit,
    val onAboutClick: () -> Unit,
    val onSettingsClick: () -> Unit,
    val onStatisticClick: () -> Unit,
    val hasContinueGame: Boolean,
    val onResumeGame: () -> Unit,
)

enum class MenuButtonType {
    PRIMARY,
    SECONDARY
}

private data class MenuItem(
    val textRes: Int,
    val icon: ImageVector,
    val contentDescriptionRes: Int,
    val onClick: () -> Unit,
    val type: MenuButtonType = MenuButtonType.SECONDARY
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
            Spacer(Modifier.weight(HeaderSpacerWeight))
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.app_icon, stringResource(R.string.app_name))
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.weight(ContentSpacerWeight))

            SudokuMainMenuItems(data) { showNewGame = true }
        }

        if (showNewGame) {
            ShowNewGame(
                sheetState = sheetState,
                data = data,
                onDismissBottom = { showNewGame = false }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ShowNewGame(
    sheetState: SheetState,
    data: SudokuMainMenuData,
    onDismissBottom: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val onDifficultySelected: (Difficulty) -> Unit = { selectedDifficulty ->
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissBottom()
                data.onStartGame(selectedDifficulty)
            }
        }
    }

    val onDismiss: () -> Unit = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissBottom()
            }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissBottom
    ) {
        DifficultySelectionSheet(
            difficulties = Difficulty.entries,
            onDifficultySelected = onDifficultySelected,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun SudokuMainMenuItems(
    data: SudokuMainMenuData,
    onNewGame: () -> Unit,
) {
    val menuItems = remember(data) {
        buildList {
            if (data.hasContinueGame) {
                add(
                    MenuItem(
                        textRes = R.string.resume_game,
                        icon = Icons.Filled.PlayArrow,
                        contentDescriptionRes = R.string.resume_game_context_desc,
                        onClick = data.onResumeGame,
                        type = MenuButtonType.PRIMARY
                    )
                )
            }
            add(
                MenuItem(
                    textRes = R.string.new_game,
                    icon = Icons.Filled.AddCircleOutline,
                    contentDescriptionRes = R.string.new_game_context_desc,
                    onClick = onNewGame,
                    type = MenuButtonType.PRIMARY
                )
            )
            add(
                MenuItem(
                    textRes = R.string.settings,
                    icon = Icons.Filled.Settings,
                    contentDescriptionRes = R.string.settings_content_desc,
                    onClick = data.onSettingsClick
                )
            )
            add(
                MenuItem(
                    textRes = R.string.statistics,
                    icon = Icons.Filled.BarChart,
                    contentDescriptionRes = R.string.statistics_content_desc,
                    onClick = data.onStatisticClick
                )
            )
            add(
                MenuItem(
                    textRes = R.string.about,
                    icon = Icons.Filled.Info,
                    contentDescriptionRes = R.string.about_content_desc,
                    onClick = data.onAboutClick
                )
            )
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.Large)
    ) {
        menuItems.forEachIndexed { index, item ->
            val isResumeButton = index == 0 && data.hasContinueGame

            if (isResumeButton) {
                AnimatedVisibility(
                    visible = true,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    MenuButton(
                        text = stringResource(item.textRes),
                        icon = item.icon,
                        onClick = item.onClick,
                        contentDescription = stringResource(item.contentDescriptionRes),
                        type = item.type
                    )
                }
            } else {
                MenuButton(
                    text = stringResource(item.textRes),
                    icon = item.icon,
                    onClick = item.onClick,
                    contentDescription = stringResource(item.contentDescriptionRes),
                    type = item.type
                )
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
    modifier: Modifier = Modifier,
    type: MenuButtonType = MenuButtonType.SECONDARY
) {
    when (type) {
        MenuButtonType.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                contentPadding = PaddingValues(horizontal = Dimens.Large, vertical = Dimens.Medium)
            ) {
                MenuButtonContent(
                    icon = icon,
                    text = text,
                    contentDescription = contentDescription
                )
            }
        }
        MenuButtonType.SECONDARY -> {
            FilledTonalButton(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = Dimens.Large, vertical = Dimens.Medium)
            ) {
                MenuButtonContent(
                    icon = icon,
                    text = text,
                    contentDescription = contentDescription
                )
            }
        }
    }
}

@Composable
private fun MenuButtonContent(
    icon: ImageVector,
    text: String,
    contentDescription: String
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
        Spacer(Modifier.width(Dimens.Large))
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontSize = 18.sp,
        )
    }
}
