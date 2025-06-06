package org.dsh.personal.sudoku.presentation.statistic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.entity.DifficultyStats
import org.dsh.personal.sudoku.domain.entity.SudokuGameStats
import org.dsh.personal.sudoku.presentation.view.Dimens
import java.text.DecimalFormat
import java.util.Locale

private const val PercentAll = 100.0

@Composable
fun StatItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.Small),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DifficultyStatsSection(difficulty: Difficulty, stats: DifficultyStats) {
    Column {
        Text(
            difficulty.name,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = Dimens.Small)
        )
        StatItem(stringResource(R.string.games_played), stats.gamesPlayed.toString())
        // Calculate and display win rate for this difficulty
        val winRate = if (stats.gamesPlayed > 0) {
            (stats.gamesWon.toDouble() / stats.gamesPlayed.toDouble()) * PercentAll
        } else {
            0.0
        }
        StatItem(stringResource(R.string.win_rate), String.format(Locale.getDefault(), "%.1f%%", winRate))
        StatItem(stringResource(R.string.avg_time), formatTime(stats.averageCompletionTimeMillis))
        StatItem(stringResource(R.string.best_time), formatTime(stats.fastestCompletionTimeMillis))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuAnalyticsScreen(onNavigateBack: () -> Unit, stats: SudokuGameStats, onClearStat: ()-> Unit) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val decimalFormat = remember { DecimalFormat("#,##0.0") }
    Scaffold(
        topBar = {
            StatisticTopBar(onNavigateBack) { showConfirmDialog = true }
        }
    ) { paddingValues ->
        StatisticConfirmDialog(showConfirmDialog, onClearStat) { showConfirmDialog = false }

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(Dimens.Large)
                .fillMaxSize()
        ) {
            // Overall Stats Section
            statOverAll(stats, decimalFormat)

            // Records Section (Overall Best Times) - Using the overall best times from SudokuGameStats
            statRecords(stats)

            // Difficulty Breakdown Section
            statDifficulties(stats)
        }
    }
}

private fun LazyListScope.statOverAll(
    stats: SudokuGameStats,
    decimalFormat: DecimalFormat
) {
    item(key = "stat_overall") {
        Text(
            stringResource(R.string.overall_stats),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = Dimens.Medium)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.Large)
        ) {
            Column(modifier = Modifier.padding(Dimens.Large)) {
                StatItem(stringResource(R.string.games_played), stats.totalGamesPlayed.toString())
                StatItem(stringResource(R.string.games_solved), stats.totalGamesWon.toString())
                // Calculate and display overall win rate
                val overallWinRate = if (stats.totalGamesPlayed > 0) {
                    (stats.totalGamesWon.toDouble() / stats.totalGamesPlayed.toDouble()) * PercentAll
                } else {
                    0.0
                }
                StatItem(
                    stringResource(R.string.win_rate),
                    "${decimalFormat.format(overallWinRate)}%"
                )
            }
        }
    }
}

private fun LazyListScope.statRecords(stats: SudokuGameStats) {
    item(key = "stat_records") {
        Text(
            text = stringResource(R.string.records),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = Dimens.Medium)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.Large)
        ) {
            Column(modifier = Modifier.padding(Dimens.Large)) {
                StatItem(
                    stringResource(R.string.best_time_easy),
                    formatTime(stats.easyStats.fastestCompletionTimeMillis)
                )
                StatItem(
                    stringResource(R.string.best_time_medium),
                    formatTime(stats.mediumStats.fastestCompletionTimeMillis)
                )
                StatItem(
                    stringResource(R.string.best_time_hard),
                    formatTime(stats.hardStats.fastestCompletionTimeMillis)
                )
                StatItem(
                    stringResource(R.string.best_time_expert),
                    formatTime(stats.expertStats.fastestCompletionTimeMillis)
                )
            }
        }
    }
}

private fun LazyListScope.statDifficulties(stats: SudokuGameStats) {
    item("stat_difficulties") {
        Text(
            stringResource(R.string.by_difficulty),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = Dimens.Medium)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.Large)
        ) {
            Column(modifier = Modifier.padding(Dimens.Large)) {
                // Use the DifficultyStatsSection Composable for each difficulty
                DifficultyStatsSection(
                    difficulty = Difficulty.EASY,
                    stats = stats.easyStats
                )
                Spacer(modifier = Modifier.height(Dimens.Large)) // Increased spacing

                DifficultyStatsSection(
                    difficulty = Difficulty.MEDIUM,
                    stats = stats.mediumStats
                )
                Spacer(modifier = Modifier.height(Dimens.Large))

                DifficultyStatsSection(
                    difficulty = Difficulty.HARD,
                    stats = stats.hardStats
                )
                Spacer(modifier = Modifier.height(Dimens.Large))

                DifficultyStatsSection(
                    difficulty = Difficulty.EXPERT,
                    stats = stats.expertStats
                )
            }
        }
    }
}

@Composable
private fun StatisticConfirmDialog(
    showConfirmDialog: Boolean,
    onClearStat: () -> Unit,
    inDismiss: () -> Unit,
) {
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = inDismiss,
            title = { Text(stringResource(R.string.confirm_reset_title)) },
            text = { Text(stringResource(R.string.confirm_reset_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearStat()
                        inDismiss()
                    }
                ) {
                    Text(stringResource(R.string.reset))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = inDismiss
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun StatisticTopBar(
    onNavigateBack: () -> Unit,
    showConfirmDialog: ()->Unit
) {
    TopAppBar(
        title = { Text(stringResource(R.string.statistic)) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        actions = {
            IconButton(onClick = showConfirmDialog) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.clear_statistics),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    )
}

@Composable
@ReadOnlyComposable
@Suppress("MagicNumber")
fun formatTime(millis: Long): String {
    if (millis == 0L || millis == Long.MAX_VALUE) {
        return stringResource(R.string.n_a)
    }
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun PreviewSudokuAnalyticsScreen() {
    val sampleStats = SudokuGameStats(
        totalGamesPlayed = 250,
        totalGamesWon = 200,

        easyStats = DifficultyStats(
            gamesPlayed = 100,
            gamesWon = 95,
            averageCompletionTimeMillis = 180000,
            fastestCompletionTimeMillis = 90000,
        ),
        mediumStats = DifficultyStats(
            gamesPlayed = 80,
            gamesWon = 65,
            averageCompletionTimeMillis = 300000,
            fastestCompletionTimeMillis = 240000,
        ),
        hardStats = DifficultyStats(
            gamesPlayed = 50,
            gamesWon = 30,
            averageCompletionTimeMillis = 600000,
            fastestCompletionTimeMillis = 480000,
        ),
        expertStats = DifficultyStats(
            gamesPlayed = 20,
            gamesWon = 10,
            averageCompletionTimeMillis = 900000,
            fastestCompletionTimeMillis = 720000,
        )
    )
    SudokuAnalyticsScreen(stats = sampleStats, onNavigateBack = {}) {

    }
}
