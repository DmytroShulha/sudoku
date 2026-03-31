package org.dsh.personal.sudoku.presentation.success

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.twotone.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.entity.SudokuBoardState
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.entity.SudokuGameStatistic
import org.dsh.personal.sudoku.presentation.capitalizeFirstLetter
import org.dsh.personal.sudoku.presentation.view.Dimens
import org.dsh.personal.sudoku.presentation.view.toFormattedString
import kotlin.time.Duration.Companion.seconds


@Composable
fun SuccessScreen(
    gameStats: SudokuGameState,
    onNewGameClicked: () -> Unit,
    onMainMenuClicked: () -> Unit,
    onShareClicked: (() -> Unit)? = null,
    modifier: Modifier
) {
    val trophyScale = remember { Animatable(0f) }
    val trophyRotation = remember { Animatable(-15f) }

    LaunchedEffect(Unit) {
        // Entrance animation with bounce
        trophyScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        // Subtle rotation for celebration effect
        trophyRotation.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.Large)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Trophy Icon with celebration animation
        Icon(
            imageVector = Icons.TwoTone.EmojiEvents,
            contentDescription = stringResource(R.string.congratulations),
            modifier = Modifier
                .size(Dimens.TrophyIconLarge)
                .graphicsLayer(
                    scaleX = trophyScale.value,
                    scaleY = trophyScale.value,
                    rotationZ = trophyRotation.value
                ),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(Dimens.XLarge))

        // Congratulatory Message
        Text(
            text = stringResource(R.string.congratulations),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.sudoku_solved),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Dimens.XXLarge))

        // Game Statistics Section
        SectionStatistic(gameStats)

        Spacer(modifier = Modifier.height(Dimens.XXXLarge))

        // Action Buttons
        GameContinueButtons(onNewGameClicked, onMainMenuClicked)

        // Optional: Share Button
        onShareClicked?.let {
            Spacer(modifier = Modifier.height(Dimens.Large))
            Button(
                onClick = it,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    stringResource(R.string.share_achievement),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun GameContinueButtons(onNewGameClicked: () -> Unit, onMainMenuClicked: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Medium)
    ) {
        // New Game Button
        Button(
            onClick = onNewGameClicked,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = Dimens.BigMedium)
        ) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = stringResource(R.string.new_game_context_desc),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                stringResource(R.string.new_game),
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Main Menu Button
        OutlinedButton(
            onClick = onMainMenuClicked,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = Dimens.BigMedium)
        ) {
            Icon(
                Icons.Filled.Home,
                contentDescription = stringResource(R.string.main_menu),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                stringResource(R.string.main_menu),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun SectionStatistic(gameStats: SudokuGameState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Game statistics summary"
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.Large),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(Dimens.BigMedium)
        ) {
            StatisticRow(
                stringResource(R.string.time_taken),
                gameStats.duration.toFormattedString()
            )
            StatisticRow(
                stringResource(R.string.difficulty_c),
                gameStats.difficulty.toString().capitalizeFirstLetter()
            )
            StatisticRow(
                stringResource(R.string.steps_taken),
                gameStats.history.size.toString()
            )
            StatisticRow(
                stringResource(R.string.notes_made),
                gameStats.boardState.grid.flatten().sumOf { it.notes.size }.toString()
            )
        }
    }
}

@Composable
fun StatisticRow(label: String, value: String) {
    // Row for displaying a single statistic
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

class SuccessScreenStateProvider : PreviewParameterProvider<SudokuGameState> {
    override val values = sequenceOf(
        SudokuGameState(
            // Quick game
            boardState = SudokuBoardState(emptyList(), emptyList(), emptyList()).apply {

            },
            difficulty = Difficulty.EASY,
            duration = 52.seconds, // 45s
            history = emptyList(),
            isSolved = true,
            gameStatistic = SudokuGameStatistic(Difficulty.EASY, 0L, 0L),
            mistakesMade = 0,
            timerMillis = 0,
            hintsRemaining = 0,
            gameId = "",
            availableNumbers = emptyList(),
            redoStack = emptyList(),
        )
    )
}

@Preview(name = "Success Screen Parameterized", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun SuccessScreenParameterizedPreview(
    @PreviewParameter(SuccessScreenStateProvider::class) gameStats: SudokuGameState
) {
    MaterialTheme {
        SuccessScreen(
            gameStats = gameStats,
            onNewGameClicked = {},
            onMainMenuClicked = {},
            onShareClicked = if (gameStats.difficulty == Difficulty.EXPERT) ({}) else null, // Show share for expert
            modifier = Modifier
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF) // White background for the row
@Composable
fun StatisticRowPreview() {
    MaterialTheme {
        Column { // Wrap in column to see multiple rows
            StatisticRow(label = "Time Taken", value = "05:32")
            StatisticRow(label = "Difficulty", value = "Medium")
            StatisticRow(label = "Steps Taken", value = "152")
        }
    }
}
