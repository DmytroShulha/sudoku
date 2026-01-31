package org.dsh.personal.sudoku.presentation.view

import android.content.Context
import android.media.AudioManager
import android.provider.SyncStateContract
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.dsh.personal.sudoku.domain.entity.SudokuCellState
import org.dsh.personal.sudoku.domain.entity.SudokuEffects

private const val BASE_FONT_SIZE_SP = 24f
private const val BOUNCE_PEAK_FONT_SIZE_SP = 30f

@Composable
fun SudokuCellView(
    cell: SudokuCellState,
    isSelected: Boolean,
    onClick: () -> Unit,
    effects: SudokuEffects
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        cell.isHighlighted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        else -> Color.Transparent
    }

    PerformEffects(cell, effects, isSelected)

    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    val animatedFontSize = remember { Animatable(BASE_FONT_SIZE_SP) }

    LaunchedEffect(isSelected, cell.value) {
        if (isSelected && !cell.isEmpty()) {
            launch {
                animatedFontSize.animateTo(
                    targetValue = BOUNCE_PEAK_FONT_SIZE_SP,
                    animationSpec = tween(durationMillis = 100, easing = LinearOutSlowInEasing)
                )
                animatedFontSize.animateTo(
                    targetValue = BASE_FONT_SIZE_SP,
                    animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing)
                )
            }
        } else {
            launch {
                animatedFontSize.snapTo(BASE_FONT_SIZE_SP)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(1.dp)
            .onGloballyPositioned { coordinates ->
                boxSize = coordinates.size
            },
        contentAlignment = Alignment.Center
    ) {
        if (!cell.isEmpty()) {
            val textColor = when {
                cell.isError -> MaterialTheme.colorScheme.error
                cell.isClue -> MaterialTheme.colorScheme.onSurface // Clues are neutral
                else -> MaterialTheme.colorScheme.primary // User input is accented
            }
            
            val fontWeight = if (cell.isClue) FontWeight.Bold else FontWeight.Medium
            
            Text(
                text = cell.value.toString(),
                color = textColor,
                fontSize = animatedFontSize.value.sp,
                style = LocalTextStyle.current.copy(
                    fontWeight = fontWeight,
                    // Remove the stroke for a cleaner enterprise look
                    shadow = null
                )
            )
        } else if (cell.notes.isNotEmpty()) {
            NotesGrid(cell.notes, boxSize)
        }
    }
}

@Composable
private fun PerformEffects(
    cell: SudokuCellState,
    effects: SudokuEffects,
    isSelected: Boolean
) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val haptic = LocalHapticFeedback.current
    when {
        cell.isError -> {
            if (effects.useSounds) {
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID)
            }
            if (effects.useHaptic) {
                haptic.performHapticFeedback(HapticFeedbackType.Reject)
            }
        }

        isSelected -> {
            if (effects.useSounds) {
                audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
            }
            if (effects.useHaptic) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }
}

// Preview for SudokuCellView
@Preview(showBackground = true)
@Composable
fun SudokuCellViewPreviewClue() {
    SudokuCellView(
        cell = SudokuCellState(id = SyncStateContract.Constants.DATA, value = 5, isClue = true),
        isSelected = false,
        onClick = {},
        effects = SudokuEffects()
    )
}

@Preview(showBackground = true)
@Composable
fun SudokuCellViewPreviewUserValue() {
    SudokuCellView(
        cell = SudokuCellState(id = SyncStateContract.Constants.DATA, value = 3, isClue = false),
        isSelected = true,
        onClick = {},
        effects = SudokuEffects()
    )
}
