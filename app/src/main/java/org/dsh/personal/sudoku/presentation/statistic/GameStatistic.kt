package org.dsh.personal.sudoku.presentation.statistic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.entity.DifficultyStats
import org.dsh.personal.sudoku.domain.entity.SudokuGameStats
import org.dsh.personal.sudoku.presentation.capitalizeFirstLetter
import org.dsh.personal.sudoku.presentation.view.Dimens
import java.text.DecimalFormat
import java.util.Locale

// Semantic colors for difficulty levels - intentionally fixed for consistent UX
private val DifficultyColorEasy = Color(0xFF4CAF50)
private val DifficultyColorMedium = Color(0xFFFF9800)
private val DifficultyColorHard = Color(0xFFF44336)
private val DifficultyColorExpert = Color(0xFF9C27B0)

private const val PercentAll = 100.0
private const val MillisInSecond = 1000L
private const val SecondsInMinute = 60L
private const val MinutesInHour = 60L

private fun calculateWinRate(won: Int, total: Int): Double {
    return if (total > 0) (won.toDouble() / total) * PercentAll else 0.0
}

@Composable
private fun getDifficultyProperties(difficulty: Difficulty): Triple<ImageVector, Color, String> {
    return when (difficulty) {
        Difficulty.EASY -> Triple(
            Icons.Filled.WbSunny,
            DifficultyColorEasy,
            difficulty.toString().capitalizeFirstLetter()
        )
        Difficulty.MEDIUM -> Triple(
            Icons.Filled.Psychology,
            DifficultyColorMedium,
            difficulty.toString().capitalizeFirstLetter()
        )
        Difficulty.HARD -> Triple(
            Icons.Filled.BatteryAlert,
            DifficultyColorHard,
            difficulty.toString().capitalizeFirstLetter()
        )
        Difficulty.EXPERT -> Triple(
            Icons.Filled.EmojiEvents,
            DifficultyColorExpert,
            difficulty.toString().capitalizeFirstLetter()
        )
    }
}

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
fun StatItemWithProgress(
    label: String,
    value: String,
    progress: Float,
    progressColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(modifier = Modifier.padding(vertical = Dimens.Small)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Small))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.Small),
            color = progressColor,
        )
    }
}

@Composable
fun DifficultyStatsSection(difficulty: Difficulty, stats: DifficultyStats) {
    val (icon, color, displayName) = getDifficultyProperties(difficulty)
    val winRate = calculateWinRate(stats.gamesWon, stats.gamesPlayed)
    val winRateProgress = (winRate / PercentAll).toFloat()

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = Dimens.Medium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.difficulty_icon_desc, displayName),
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.Medium))
            Text(
                displayName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }

        StatItem(stringResource(R.string.games_played), stats.gamesPlayed.toString())

        StatItemWithProgress(
            label = stringResource(R.string.win_rate),
            value = String.format(Locale.getDefault(), "%.1f%%", winRate),
            progress = winRateProgress,
            progressColor = color
        )

        StatItem(stringResource(R.string.avg_time), formatTime(stats.averageCompletionTimeMillis))
        StatItem(stringResource(R.string.best_time), formatTime(stats.fastestCompletionTimeMillis))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuAnalyticsScreen(
    onNavigateBack: () -> Unit,
    stats: SudokuGameStats?,
    onClearStat: () -> Unit,
    isLoading: Boolean = false
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val decimalFormat = remember { DecimalFormat("#,##0.0") }

    Scaffold(
        topBar = {
            StatisticTopBar(
                onNavigateBack = onNavigateBack,
                showConfirmDialog = { showConfirmDialog = true },
                hasStats = stats != null && stats.totalGamesPlayed > 0
            )
        }
    ) { paddingValues ->
        if (showConfirmDialog) {
            StatisticConfirmDialog(onClearStat) { showConfirmDialog = false }
        }

        when {
            isLoading -> {
                LoadingState(paddingValues)
            }
            stats == null || stats.totalGamesPlayed == 0 -> {
                EmptyState(paddingValues, onNavigateBack)
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(Dimens.Large)
                        .fillMaxSize()
                ) {
                    statOverAll(stats, decimalFormat)
                    statRecords(stats)
                    statDifficulties(stats)
                }
            }
        }
    }
}

@Composable
private fun LoadingState(paddingValues: androidx.compose.foundation.layout.PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimens.Large))
            Text(
                text = stringResource(R.string.loading_statistics),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EmptyState(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(Dimens.Large),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.Large)
        ) {
            Icon(
                imageVector = Icons.Filled.Leaderboard,
                contentDescription = stringResource(R.string.leaderboard_icon_desc),
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Text(
                text = stringResource(R.string.no_games_played),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.start_playing_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimens.Medium))
            Button(onClick = onNavigateBack) {
                Text(stringResource(R.string.play_now))
            }
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
            fontWeight = FontWeight.Bold,
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

                val overallWinRate = calculateWinRate(stats.totalGamesWon, stats.totalGamesPlayed)
                val winRateProgress = (overallWinRate / PercentAll).toFloat()

                StatItemWithProgress(
                    label = stringResource(R.string.win_rate),
                    value = "${decimalFormat.format(overallWinRate)}%",
                    progress = winRateProgress
                )
            }
        }
    }
}

private fun LazyListScope.statRecords(stats: SudokuGameStats) {
    val recordsWithData = buildList {
        if (stats.easyStats.gamesPlayed > 0) {
            add(Difficulty.EASY to stats.easyStats)
        }
        if (stats.mediumStats.gamesPlayed > 0) {
            add(Difficulty.MEDIUM to stats.mediumStats)
        }
        if (stats.hardStats.gamesPlayed > 0) {
            add(Difficulty.HARD to stats.hardStats)
        }
        if (stats.expertStats.gamesPlayed > 0) {
            add(Difficulty.EXPERT to stats.expertStats)
        }
    }

    if (recordsWithData.isEmpty()) return

    item(key = "stat_records") {
        Text(
            text = stringResource(R.string.records),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = Dimens.Medium)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.Large)
        ) {
            Column(
                modifier = Modifier.padding(Dimens.Large),
                verticalArrangement = Arrangement.spacedBy(Dimens.Small)
            ) {
                recordsWithData.forEach { (difficulty, difficultyStats) ->
                    RecordItem(
                        difficulty = difficulty,
                        time = formatTime(difficultyStats.fastestCompletionTimeMillis)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordItem(difficulty: Difficulty, time: String) {
    val (icon, color, displayName) = getDifficultyProperties(difficulty)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.Small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Medium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.difficulty_icon_desc, displayName),
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = time,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun LazyListScope.statDifficulties(stats: SudokuGameStats) {
    val difficultiesWithData = buildList {
        if (stats.easyStats.gamesPlayed > 0) {
            add(Difficulty.EASY to stats.easyStats)
        }
        if (stats.mediumStats.gamesPlayed > 0) {
            add(Difficulty.MEDIUM to stats.mediumStats)
        }
        if (stats.hardStats.gamesPlayed > 0) {
            add(Difficulty.HARD to stats.hardStats)
        }
        if (stats.expertStats.gamesPlayed > 0) {
            add(Difficulty.EXPERT to stats.expertStats)
        }
    }

    if (difficultiesWithData.isEmpty()) return

    item("stat_difficulties") {
        Text(
            stringResource(R.string.by_difficulty),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = Dimens.Medium)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.Large)
        ) {
            Column(
                modifier = Modifier.padding(Dimens.Large),
                verticalArrangement = Arrangement.spacedBy(Dimens.Large)
            ) {
                difficultiesWithData.forEach { (difficulty, difficultyStats) ->
                    DifficultyStatsSection(
                        difficulty = difficulty,
                        stats = difficultyStats
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticConfirmDialog(
    onClearStat: () -> Unit,
    inDismiss: () -> Unit,
) {
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun StatisticTopBar(
    onNavigateBack: () -> Unit,
    showConfirmDialog: () -> Unit,
    hasStats: Boolean
) {
    TopAppBar(
        title = {
            Text(
                stringResource(R.string.statistic),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        ),
        actions = {
            if (hasStats) {
                IconButton(onClick = showConfirmDialog) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.clear_statistics),
                    )
                }
            }
        }
    )
}

@Composable
@ReadOnlyComposable
fun formatTime(millis: Long): String {
    if (millis == 0L || millis == Long.MAX_VALUE) {
        return stringResource(R.string.n_a)
    }

    val totalSeconds = millis / MillisInSecond
    val hours = totalSeconds / (SecondsInMinute * MinutesInHour)
    val minutes = (totalSeconds % (SecondsInMinute * MinutesInHour)) / SecondsInMinute
    val seconds = totalSeconds % SecondsInMinute

    return when {
        hours > 0 -> String.format(Locale.getDefault(), "%dh %dm %ds", hours, minutes, seconds)
        minutes > 0 -> String.format(Locale.getDefault(), "%dm %ds", minutes, seconds)
        else -> String.format(Locale.getDefault(), "%ds", seconds)
    }
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
    SudokuAnalyticsScreen(
        stats = sampleStats,
        onNavigateBack = {},
        onClearStat = {},
        isLoading = false
    )
}
