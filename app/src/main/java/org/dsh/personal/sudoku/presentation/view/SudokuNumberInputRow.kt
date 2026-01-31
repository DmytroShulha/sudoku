package org.dsh.personal.sudoku.presentation.view

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ModeEdit
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    modifier: Modifier = Modifier, data: SudokuNumberInputRowData
) {

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        SudokuGameButtonsRow(
            modifier,
            currentInputMode = data.currentInputMode,
            notesClick = data.notesClick,
            undoClick = data.undoClick,
            onNumberClick = data.onNumberClick
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            data.numbers.forEach { numberState ->
                NumberInputButton(
                    numberState = numberState, onNumberClick = data.onNumberClick
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
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        SudokuFunctionalButton(
            modifier = Modifier.weight(1f),
            colors = currentInputMode.buttonColorsColors(),
            buttonClick = notesClick,
            data = SudokuFunctionalButtonData(
                icon = currentInputMode.buttonNoteIcon(),
                contentDescription = R.string.notes_content_desc,
                text = R.string.notes
            )
        )

        SudokuFunctionalButton(
            modifier = Modifier.weight(1f),
            data = SudokuFunctionalButtonData(
                icon = Icons.AutoMirrored.Outlined.Undo,
                contentDescription = R.string.undo,
                text = R.string.undo
            ),
            buttonClick = undoClick,
        )

        SudokuFunctionalButton(
            modifier = Modifier.weight(1f),
            data = SudokuFunctionalButtonData(
                icon = Icons.Outlined.Clear,
                contentDescription = R.string.clear_content_desc,
                text = R.string.clear
            ),
            buttonClick = { onNumberClick(0) },
        )
    }
}

@Composable
fun InputMode.buttonNoteIcon() = when (this) {
    InputMode.NOTES -> Icons.Default.EditNote
    InputMode.VALUE -> Icons.Default.ModeEdit
}

@Composable
fun InputMode.buttonColorsColors() = if (this == InputMode.NOTES) {
    ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
} else {
    ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}


data class SudokuFunctionalButtonData(
    val icon: ImageVector,
    @StringRes val contentDescription: Int,
    @StringRes val text: Int,
)

@Composable
private fun SudokuFunctionalButton(
    data: SudokuFunctionalButtonData,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    buttonClick: () -> Unit,
) {
    Button(
        onClick = buttonClick,
        modifier = modifier.height(48.dp),
        colors = colors,
        shape = CircleShape,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = stringResource(data.contentDescription),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(data.text),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun RowScope.NumberInputButton(
    numberState: SudokuNumberButtonState, onNumberClick: (Int) -> Unit
) {

    var countChanged by remember { mutableStateOf(false) }
    LaunchedEffect(numberState.availableCount) {
        countChanged = true
        delay(ANIMATION_DURATION_300.toLong())
        countChanged = false
    }

    val textColor by animateColorAsState(
        targetValue = if (numberState.isPossible) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        }, animationSpec = tween(durationMillis = ANIMATION_DURATION_300)
    )
    val boxColor by animateColorAsState(
        targetValue = if (numberState.isPossible) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        }, animationSpec = tween(durationMillis = ANIMATION_DURATION_300)
    )


    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val countColor by animateColorAsState(
        targetValue = if (countChanged) colorPrimary else colorOnSurfaceVariant,
        animationSpec = keyframes {
            durationMillis = ANIMATION_DURATION_200
            colorPrimary at 0
            colorOnSurfaceVariant at ANIMATION_DURATION_200
        })

    Box(
        modifier = Modifier
            .weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable { onNumberClick(numberState.number) },
            shape = CircleShape,
            color = boxColor,
            tonalElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = numberState.number.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = textColor
                )
            }
        }

        if (numberState.availableCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(18.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    @Suppress("DEPRECATION")
                    Text(
                        text = numberState.availableCount.toString(),
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = countColor,
                            textAlign = TextAlign.Center,
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
            }
        }
    }

}

@Preview(
    showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES
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
