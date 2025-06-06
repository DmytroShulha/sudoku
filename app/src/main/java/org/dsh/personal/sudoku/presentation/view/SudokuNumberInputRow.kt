package org.dsh.personal.sudoku.presentation.view

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.domain.entity.InputMode
import org.dsh.personal.sudoku.domain.entity.SudokuNumberButtonState
import org.dsh.personal.sudoku.theme.PersonalTheme
import kotlin.collections.List

private const val ANIMATION_DURATION_200 = 200
private const val ANIMATION_DURATION_300 = 300

data class SudokuNumberInputRowData(
    val numbers: List<SudokuNumberButtonState>,
    val onNumberClick: (Int) -> Unit,
    val undoClick: () -> Unit,
    val notesClick: () -> Unit,
    val currentInputMode: InputMode,
)

@Composable
fun SudokuNumberInputRow(
    modifier: Modifier = Modifier,
    data: SudokuNumberInputRowData
) {

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        SudokuGameButtonsRow(
            modifier,
            data.currentInputMode,
            data.notesClick,
            data.undoClick,
            data.onNumberClick
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            data.numbers.forEach { numberState ->
                NumberInputButton(
                    numberState = numberState,
                    onNumberClick = data.onNumberClick
                )
            }
        }
    }
}

@Composable
private fun SudokuGameButtonsRow(
    modifier: Modifier,
    currentInputMode: InputMode,
    notesClick: () -> Unit,
    undoClick: () -> Unit,
    onNumberClick: (Int) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        SudokuFunctionalButton(
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentInputMode == InputMode.NOTES) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.primary
                }
            ), buttonClick = notesClick, data = SudokuFunctionalButtonData(
                icon = Icons.Default.EditNote,
                contentDescription = R.string.notes_content_desc,
                text = R.string.notes
            )
        )

        SudokuFunctionalButton(
            data = SudokuFunctionalButtonData(
                icon = Icons.AutoMirrored.Outlined.Undo,
                contentDescription = R.string.undo,
                text = R.string.undo
            ),
            buttonClick = undoClick,

            )

        SudokuFunctionalButton(
            data = SudokuFunctionalButtonData(
                icon = Icons.Outlined.Clear,
                contentDescription = R.string.clear_content_desc,
                text = R.string.clear
            ),
            buttonClick = { onNumberClick(0) },

            )
    }
}

data class SudokuFunctionalButtonData(
    val icon: ImageVector,
    @StringRes
    val contentDescription: Int,
    @StringRes
    val text: Int,
)

@Composable
private fun SudokuFunctionalButton(
    data: SudokuFunctionalButtonData,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    buttonClick: () -> Unit,
) {
    Button(
        onClick = buttonClick,
        colors = colors
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = data.icon,
                contentDescription = stringResource(data.contentDescription)
            )
            Spacer(Modifier.width(4.dp))
            Text(stringResource(data.text))
        }
    }
}

@Composable
fun NumberInputButton(
    numberState: SudokuNumberButtonState,
    onNumberClick: (Int) -> Unit
) {

    var countChanged by remember { mutableStateOf(false) }
    LaunchedEffect(numberState.availableCount) {
        countChanged = true
        delay(ANIMATION_DURATION_300.toLong())
        countChanged = false
    }

    val textColor by animateColorAsState(
        targetValue = if (numberState.isPossible) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSecondary
        },
        animationSpec = tween(durationMillis = ANIMATION_DURATION_300)
    )
    val boxColor by animateColorAsState(
        targetValue = if (numberState.isPossible) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondary
        },
        animationSpec = tween(durationMillis = ANIMATION_DURATION_300)
    )


    val colorOutline = MaterialTheme.colorScheme.outline
    val colorOutlineVariant = MaterialTheme.colorScheme.outlineVariant
    val countColor by animateColorAsState(
        targetValue = if (countChanged) colorOutline else colorOutlineVariant,
        animationSpec = keyframes {
            durationMillis = ANIMATION_DURATION_200
            colorOutline at 0
            colorOutlineVariant at ANIMATION_DURATION_200
        }
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(boxColor)
            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
            .clickable { onNumberClick(numberState.number) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = numberState.number.toString(),
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            color = textColor
        )

        Text(
            text = numberState.availableCount.toString(),
            fontSize = 10.sp,
            color = countColor,
            lineHeight = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 2.dp, end = 3.dp)
        )
    }

}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
@Suppress("MagicNumber")
fun PreviewNumberInputRow() {
    PersonalTheme {
        val sampleNumbers = listOf(
            SudokuNumberButtonState(1, true, 5),
            SudokuNumberButtonState(2, false, 0),
            SudokuNumberButtonState(3, true, 2),
            SudokuNumberButtonState(4, true, 8),
            SudokuNumberButtonState(5, true, 1),
            SudokuNumberButtonState(6, true, 0),
            SudokuNumberButtonState(7, true, 3),
            SudokuNumberButtonState(8, true, 6),
            SudokuNumberButtonState(9, true, 4)
        )
        Surface {
            SudokuNumberInputRow(
                data = SudokuNumberInputRowData(
                    numbers = sampleNumbers,
                    onNumberClick = {},
                    undoClick = {},
                    notesClick = {},
                    currentInputMode = InputMode.VALUE
                )
            )
        }
    }
}
