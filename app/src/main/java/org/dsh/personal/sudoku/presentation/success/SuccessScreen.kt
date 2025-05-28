package org.dsh.personal.sudoku.presentation.success

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.entity.SudokuBoardState
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.entity.SudokuGameStatistic
import org.dsh.personal.sudoku.presentation.capitalizeFirstLetter
import kotlin.time.Duration.Companion.seconds


@Composable
fun SuccessScreen(
    gameStats: SudokuGameState,
    onNewGameClicked: () -> Unit,
    onMainMenuClicked: () -> Unit,
    onShareClicked: (() -> Unit)? = null,
    modifier: Modifier
) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Trophy Icon
            Icon(
                imageVector = Icons.TwoTone.EmojiEvents,
                contentDescription = stringResource(R.string.congratulations),
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Congratulatory Message
            Text(
                text = stringResource(R.string.congratulations),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.sudoku_solved),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Game Statistics Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatisticRow(stringResource(R.string.time_taken), gameStats.duration.toString())
                    StatisticRow(stringResource(R.string.difficulty_c), gameStats.difficulty.toString().capitalizeFirstLetter())
                    StatisticRow(stringResource(R.string.steps_taken), gameStats.history.size.toString())
                    StatisticRow(stringResource(R.string.notes_made), gameStats.boardState.grid.flatten().sumOf { it.notes.size }.toString())
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // New Game Button
                Button(
                    onClick = onNewGameClicked,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.new_game_context_desc), modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.new_game), fontSize = 16.sp)
                }

                // Main Menu Button
                OutlinedButton(
                    onClick = onMainMenuClicked,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Filled.Home, contentDescription = stringResource(R.string.main_menu), modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.main_menu), fontSize = 16.sp)
                }
            }

            // Optional: Share Button
            onShareClicked?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = it,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.share_achievement), fontSize = 16.sp)
                }
            }
        }

}

@Composable
fun StatisticRow(label: String, value: String) {
    // Row for displaying a single statistic
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
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