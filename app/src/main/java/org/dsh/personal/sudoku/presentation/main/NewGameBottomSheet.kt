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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.presentation.capitalizeFirstLetter
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.stringResource
import org.dsh.personal.sudoku.R

@Composable
fun DifficultySelectionSheet(
    difficulties: List<Difficulty>,
    onDifficultySelected: (Difficulty) -> Unit,
    onDismiss: () -> Unit, // Callback to dismiss the sheet
    modifier: Modifier = Modifier
) {
    // Container for the bottom sheet content
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding(), // Handles insets for navigation bars
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title for the bottom sheet
        Text(
            stringResource(R.string.select_difficulty),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Scrollable list of difficulty options
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(difficulties) { difficulty ->
                DifficultyItem(
                    difficulty = difficulty,
                    onClick = { onDifficultySelected(difficulty) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.cancel))
        }
    }
}

@Composable
fun DifficultyItem(
    difficulty: Difficulty,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp), // Increased vertical padding for better touch target
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = difficulty.toString().capitalizeFirstLetter(),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp // Slightly larger font for readability
            )
            Icon(
                imageVector = Icons.Outlined.RadioButtonUnchecked,
                contentDescription = stringResource(
                    R.string.select_difficulty_level,
                    difficulty.toString().capitalizeFirstLetter()
                ),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
