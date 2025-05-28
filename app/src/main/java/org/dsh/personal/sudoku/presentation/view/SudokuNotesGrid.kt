package org.dsh.personal.sudoku.presentation.view

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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsh.personal.sudoku.domain.entity.SudokuCellNote

@Composable
fun NotesGrid(notes: Set<SudokuCellNote>, boxSize: IntSize) {

    val density = LocalDensity.current // Get the current density
    val boxSizeDp: Dp = remember(boxSize) { with(density) { ((boxSize.width.toDp() - 8.dp).toPx() / 3).toDp() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp), // Add some padding around the grid
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Center the grid vertically
    ) {
        // Row 1: 1 2 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween // Space out the numbers
        ) {
            NoteText(number = 1, notes = notes, boxSizeDp)
            NoteText(number = 2, notes = notes, boxSizeDp)
            NoteText(number = 3, notes = notes, boxSizeDp)
        }
        Spacer(modifier = Modifier.height(2.dp)) // Add vertical space between rows
        // Row 2: 4 5 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NoteText(number = 4, notes = notes, boxSizeDp)
            NoteText(number = 5, notes = notes, boxSizeDp)
            NoteText(number = 6, notes = notes, boxSizeDp)
        }
        Spacer(modifier = Modifier.height(2.dp)) // Add vertical space
        // Row 3: 7 8 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NoteText(number = 7, notes = notes, boxSizeDp)
            NoteText(number = 8, notes = notes, boxSizeDp)
            NoteText(number = 9, notes = notes, boxSizeDp)
        }
    }
}


@Composable
private fun NoteText(number: Int, notes: Set<SudokuCellNote>, boxDp: Dp) {
    val exists = remember(notes) { notes.firstOrNull { it.value == number } }
    if (exists != null && exists.isVisible) {
        val color = when {
            exists.isError -> MaterialTheme.colorScheme.error
            exists.isHighlighted -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.secondary
        }
        val textStyle = LocalTextStyle.current
        val minTextSize: TextUnit = 1.sp

        Box(modifier = Modifier.size(boxDp)) {
            var autoCalculatedTextSize by remember { mutableStateOf(textStyle.fontSize.takeIf { it.isSp } ?: 20.sp) }

            Text(
                text = number.toString(),
                fontSize = autoCalculatedTextSize,
                modifier = Modifier.fillMaxSize(),
                color = color,
                textAlign = TextAlign.Center,
                fontWeight = if(exists.isError || exists.isHighlighted) FontWeight.Bold else null,
                lineHeight = autoCalculatedTextSize,
                textDecoration = textStyle.textDecoration,
                fontFamily = textStyle.fontFamily,
                letterSpacing = textStyle.letterSpacing,
                maxLines = 1,
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow) {
                        val reductionFactor = 0.95f
                        val newTextSize = autoCalculatedTextSize * reductionFactor

                        autoCalculatedTextSize = if (newTextSize >= minTextSize) {
                            newTextSize
                        } else {
                            minTextSize
                        }
                    }
                }
            )
        }

    } else {
        // If the number is not in the set, display an empty space to maintain grid structure
        Spacer(modifier = Modifier.size(boxDp))
    }
}
