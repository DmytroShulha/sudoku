package org.dsh.personal.sudoku.presentation.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.dsh.personal.sudoku.domain.BLOCK_SIZE
import org.dsh.personal.sudoku.domain.ROW_SIZE
import org.dsh.personal.sudoku.domain.entity.SudokuCellState
import org.dsh.personal.sudoku.domain.entity.SudokuEffects
import org.dsh.personal.sudoku.presentation.SudokuViewModel
import org.dsh.personal.sudoku.theme.PersonalTheme
import org.dsh.personal.sudoku.utility.initializeEmptyGame


@Composable
fun SudokuBoardView(
    selectedCellPosition: Pair<Int, Int>?, // (row, col)
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
    uiState: SudokuViewModel.SudokuUiState
) {
    val thickLineDp = 2.dp
    val effects = remember(uiState.effects) { uiState.effects }

    // One UI 8.5 Container Style: Rounded Card with soft shadow
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = 2.dp, shape = RoundedCornerShape(16.dp), clip = false
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(thickLineDp / 2) // Slight padding to show the border properly
        ) {
            Column(Modifier.fillMaxSize()) {
                SudokuBoardRow(
                    uiState.game.boardState.grid,
                    selectedCellPosition,
                    onCellClick,
                    effects,
                )
            }
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
    val thinLineDp = 0.5.dp
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val solidLineColor = MaterialTheme.colorScheme.outline

    for (rowIndex in 0 until boardSize) {
        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f)
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
                color = if ((rowIndex + 1) % subgridSize == 0) solidLineColor else lineColor,
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
    val thinLineDp = 0.5.dp
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val solidLineColor = MaterialTheme.colorScheme.outline

    val isSelected by remember(data, data.selectedCellPosition) {
        derivedStateOf {
            data.selectedCellPosition?.first == data.rowIndex && data.selectedCellPosition.second == data.colIndex
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
            color = if ((data.colIndex + 1) % subgridSize == 0) solidLineColor else lineColor
        )
    }
}

@Composable
fun VerticalDivider(
    thickness: Dp, color: Color, modifier: Modifier = Modifier
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
    thickness: Dp, color: Color, modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color = color)
    )
}

@Composable
@Suppress("MagicNumber")
private fun SudokuBoardPreviewContent() {

    Surface {
        Box(Modifier.padding(16.dp)) { // Add some padding around the board for the preview
            SudokuBoardView(
                selectedCellPosition = Pair(1, 1), // Example selected cell
                onCellClick = { _, _ ->
                }, uiState = SudokuViewModel.SudokuUiState(game = initializeEmptyGame())
            )
        }
    }
}

// Multipreview for SudokuBoardView
@Preview(
    name = "Dark Mode",
    showBackground = true,
    widthDp = 360,
    heightDp = 420,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES or android.content.res.Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    widthDp = 360,
    heightDp = 420,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO or android.content.res.Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun SudokuBoardViewPreview() {
    PersonalTheme {
        SudokuBoardPreviewContent()
    }
}
