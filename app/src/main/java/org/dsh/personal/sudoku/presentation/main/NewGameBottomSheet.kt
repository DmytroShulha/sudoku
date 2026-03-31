package org.dsh.personal.sudoku.presentation.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.presentation.capitalizeFirstLetter
import org.dsh.personal.sudoku.presentation.view.Dimens

// Semantic colors for difficulty levels - intentionally fixed for consistent UX
private val DifficultyColorEasy = Color(0xFF4CAF50)
private val DifficultyColorMedium = Color(0xFFFF9800)
private val DifficultyColorHard = Color(0xFFF44336)
private val DifficultyColorExpert = Color(0xFF9C27B0)

@Composable
fun DifficultySelectionSheet(
    difficulties: List<Difficulty>,
    onDifficultySelected: (Difficulty) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimens.Large)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.select_difficulty),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = Dimens.Large)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.BigMedium)
        ) {
            items(difficulties) { difficulty ->
                DifficultyItem(
                    difficulty = difficulty,
                    onClick = { onDifficultySelected(difficulty) }
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.Large))
        TextButton(onClick = onDismiss) {
            Text(
                stringResource(R.string.cancel),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun DifficultyItem(
    difficulty: Difficulty,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconTint, description) = getDifficultyProperties(difficulty)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.VerySmall),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Large)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(
                    R.string.difficulty_icon_desc,
                    difficulty.toString().capitalizeFirstLetter()
                ),
                modifier = Modifier.size(40.dp),
                tint = iconTint
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.Small)
            ) {
                Text(
                    text = difficulty.toString().capitalizeFirstLetter(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    fontSize = 18.sp
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun getDifficultyProperties(difficulty: Difficulty): Triple<ImageVector, Color, String> {
    return when (difficulty) {
        Difficulty.EASY -> Triple(
            Icons.Filled.WbSunny,
            DifficultyColorEasy,
            stringResource(R.string.difficulty_easy_desc)
        )
        Difficulty.MEDIUM -> Triple(
            Icons.Filled.Psychology,
            DifficultyColorMedium,
            stringResource(R.string.difficulty_medium_desc)
        )
        Difficulty.HARD -> Triple(
            Icons.Filled.BatteryAlert,
            DifficultyColorHard,
            stringResource(R.string.difficulty_hard_desc)
        )
        Difficulty.EXPERT -> Triple(
            Icons.Filled.EmojiEvents,
            DifficultyColorExpert,
            stringResource(R.string.difficulty_expert_desc)
        )
    }
}
