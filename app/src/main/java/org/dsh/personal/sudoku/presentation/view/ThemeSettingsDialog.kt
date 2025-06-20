package org.dsh.personal.sudoku.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.domain.entity.SudokuBoardTheme
import org.dsh.personal.sudoku.presentation.settings.SettingSwitcher

@Composable
fun ThemeSettingsDialog(
    currentTheme: SudokuBoardTheme,
    onThemeChange: (SudokuBoardTheme) -> Unit,
    onDismissRequest: () -> Unit // Lambda to dismiss the dialog
) {
    // State to hold the current settings within the dialog
    var useSystemSetting by remember { mutableStateOf(currentTheme.useSystem) }
    var isDarkSetting by remember { mutableStateOf(currentTheme.isDark) }
    var isDynamicSetting by remember { mutableStateOf(currentTheme.isDynamic) }

    AlertDialog(
        onDismissRequest = onDismissRequest, // Dismiss when clicking outside
        title = {
            Text(stringResource(R.string.theme_settings))
        }, text = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Use System Theme Switch
                SettingSwitcher(
                    title = R.string.use_system_default,
                    checked = useSystemSetting,
                    onCheckedChange = { useSystemSetting = it })

                // Is Dark Theme Switch (Enabled only when Use System is false)
                SettingSwitcher(
                    title = R.string.dark_theme,
                    checked = isDarkSetting,
                    onCheckedChange = { isDarkSetting = it },
                    enabled = !useSystemSetting
                )

                // Is Dynamic Color Switch (Enabled only when Use System is false)
                SettingSwitcher(
                    title = R.string.dynamic_color_material_3,
                    checked = isDynamicSetting,
                    onCheckedChange = { isDynamicSetting = it })
            }
        }, confirmButton = {
            Button(
                onClick = {
                    onThemeChange(
                        SudokuBoardTheme(
                            useSystem = useSystemSetting,
                            isDark = isDarkSetting,
                            isDynamic = isDynamicSetting
                        )
                    )
                    onDismissRequest()
                }) {
                Text(stringResource(R.string.apply))
            }
        }, dismissButton = {
            Button(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.cancel))
            }
        })
}

@Preview(showBackground = true)
@Composable
fun PreviewThemeSettingsDialog() {
    ThemeSettingsDialog(
        currentTheme = SudokuBoardTheme(useSystem = true),
        onThemeChange = {},
        onDismissRequest = {})
}
