package org.dsh.personal.sudoku.presentation.view

import android.provider.SyncStateContract
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.dsh.personal.sudoku.domain.BLOCK_SIZE
import org.dsh.personal.sudoku.domain.ROW_SIZE
import org.dsh.personal.sudoku.domain.entity.SudokuCellNote
import org.dsh.personal.sudoku.domain.entity.SudokuCellState
import org.dsh.personal.sudoku.domain.entity.SudokuEffects
import org.dsh.personal.sudoku.presentation.SudokuViewModel
import org.dsh.personal.sudoku.theme.PersonalTheme


@Composable
fun SudokuBoardView(
    board: List<List<SudokuCellState>>,
    selectedCellPosition: Pair<Int, Int>?, // (row, col)
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
    settings: SudokuViewModel.SudokuSettings
) {
    val thickLineDp = 2.dp
    val lineColor = MaterialTheme.colorScheme.tertiary

    val effects = remember(settings.effects) { settings.effects }

    Box(
        modifier = modifier
            .aspectRatio(1f) // Ensure the whole board is square
            .border(thickLineDp, lineColor) // Outer border for the entire 9x9 grid
    ) {
        Column(Modifier.fillMaxSize()) {
            SudokuBoardRow(
                board,
                selectedCellPosition,
                onCellClick,
                effects,
            )
        }
    }
}

@Composable
private fun ColumnScope.SudokuBoardRow(
    board: List<List<SudokuCellState>>,
    selectedCellPosition: Pair<Int, Int>?,
    onCellClick: (Int, Int) -> Unit,
    effects: SudokuEffects,
) {
    val boardSize = ROW_SIZE
    val subgridSize = BLOCK_SIZE
    val thickLineDp = 2.dp
    val thinLineDp = 1.dp
    val lineColor = MaterialTheme.colorScheme.tertiary

    for (rowIndex in 0 until boardSize) {
        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f) // Each row takes equal height
        ) {
            for (colIndex in 0 until boardSize) {
                val cellState = board[rowIndex][colIndex]
                BoardRowCellItem(
                    data = BoardRowCellItemData(
                        cellState = cellState,
                        selectedCellPosition = selectedCellPosition,
                        rowIndex = rowIndex,
                        colIndex = colIndex,
                        onCellClick = onCellClick,
                        effects = effects,
                    )
                )
            }
        }
        // Horizontal Divider (except for the last row)
        if (rowIndex < boardSize - 1) {
            HorizontalDivider(
                thickness = if ((rowIndex + 1) % subgridSize == 0) thickLineDp else thinLineDp,
                color = lineColor
            )
        }
    }
}

data class BoardRowCellItemData(
    val cellState: SudokuCellState,
    val selectedCellPosition: Pair<Int, Int>?,
    val rowIndex: Int,
    val colIndex: Int,
    val onCellClick: (Int, Int) -> Unit,
    val effects: SudokuEffects,
)

@Composable
private fun RowScope.BoardRowCellItem(
    data: BoardRowCellItemData,
) {
    val boardSize = ROW_SIZE
    val subgridSize = BLOCK_SIZE
    val thickLineDp = 2.dp
    val thinLineDp = 1.dp
    val lineColor = MaterialTheme.colorScheme.tertiary

    val isSelected by remember(data, data.selectedCellPosition) {
        derivedStateOf {
            data.selectedCellPosition?.first == data.rowIndex &&
                    data.selectedCellPosition.second == data.colIndex
        }
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
    ) {
        SudokuCellView(
            cell = data.cellState,
            isSelected = isSelected,
            onClick = { data.onCellClick(data.rowIndex, data.colIndex) },
            effects = data.effects,
        )
    }

    // Vertical Divider (except for the last cell in a row)
    if (data.colIndex < boardSize - 1) {
        VerticalDivider(
            thickness = if ((data.colIndex + 1) % subgridSize == 0) thickLineDp else thinLineDp,
            color = lineColor
        )
    }
}

@Composable
fun VerticalDivider(
    thickness: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxHeight()
            .width(thickness)
            .background(color = color)
    )
}

@Composable
fun HorizontalDivider(
    thickness: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color = color)
    )
}

// Preview for SudokuBoardView
@Preview(
    showBackground = true, widthDp = 360, heightDp = 420,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
            or android.content.res.Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
@Suppress("MagicNumber")
fun SudokuBoardViewPreview() {
    // Create a sample board for preview
    val sampleBoard = List(9) { rowIndex ->
        List(9) { colIndex ->
            val value = when {
                rowIndex == 0 && colIndex == 0 -> 5 // Clue
                rowIndex == 1 && colIndex == 1 -> 3 // User entered
                rowIndex == 2 && colIndex == 2 -> 8 // Clue with error (for testing)
                rowIndex == 3 && colIndex == 3 -> 0 // Empty with notes
                else -> 0 // Empty
            }
            val isClue = (rowIndex == 0 && colIndex == 0) || (rowIndex == 2 && colIndex == 2)
            val notes = if (rowIndex == 3 && colIndex == 3) setOf(
                SudokuCellNote(1),
                SudokuCellNote(4),
                SudokuCellNote(6)
            ) else emptySet()
            val isError = rowIndex == 2 && colIndex == 2 // Example error

            SudokuCellState(
                id = SyncStateContract.Constants.DATA,
                value = value,
                isClue = isClue,
                notes = notes,
                isError = isError
            )
        }
    }

    PersonalTheme {
        Surface {
            Box(Modifier.padding(16.dp)) { // Add some padding around the board for the preview
                SudokuBoardView(
                    board = sampleBoard,
                    selectedCellPosition = Pair(1, 1), // Example selected cell
                    onCellClick = { _, _ ->
                    },
                    settings = SudokuViewModel.SudokuSettings()
                )
            }
        }
    }
}


// Preview for SudokuBoardView
@Suppress("MagicNumber")
@Preview(showBackground = true, widthDp = 360, heightDp = 420)
@Composable
fun SudokuBoardViewPreviewLight() {
    // Create a sample board for preview
    val sampleBoard = List(9) { rowIndex ->
        List(9) { colIndex ->
            val value = when {
                rowIndex == 0 && colIndex == 0 -> 5 // Clue
                rowIndex == 1 && colIndex == 1 -> 3 // User entered
                rowIndex == 2 && colIndex == 2 -> 8 // Clue with error (for testing)
                rowIndex == 3 && colIndex == 3 -> 0 // Empty with notes
                else -> 0 // Empty
            }
            val isClue = (rowIndex == 0 && colIndex == 0) || (rowIndex == 2 && colIndex == 2)
            val notes = if (rowIndex == 3 && colIndex == 3) setOf(
                SudokuCellNote(1),
                SudokuCellNote(4),
                SudokuCellNote(6)
            ) else emptySet()
            val isError = rowIndex == 2 && colIndex == 2 // Example error

            SudokuCellState(
                id = SyncStateContract.Constants.DATA,
                value = value,
                isClue = isClue,
                notes = notes,
                isError = isError
            )
        }
    }

    PersonalTheme {
        Surface {
            Box(Modifier.padding(16.dp)) { // Add some padding around the board for the preview
                SudokuBoardView(
                    board = sampleBoard,
                    selectedCellPosition = Pair(1, 1), // Example selected cell
                    onCellClick = { _, _ ->
                    },
                    settings = SudokuViewModel.SudokuSettings()
                )
            }
        }
    }
}

