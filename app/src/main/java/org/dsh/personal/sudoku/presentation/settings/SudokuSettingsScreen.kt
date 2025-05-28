package org.dsh.personal.sudoku.presentation.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.domain.entity.SudokuBoardTheme
import org.dsh.personal.sudoku.domain.entity.SudokuEffects
import org.dsh.personal.sudoku.presentation.SudokuViewModel
import org.dsh.personal.sudoku.presentation.view.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuSettingsScreen(
    settings: SudokuViewModel.SudokuSettings,
    onBackClick: () -> Unit,
    onSaveSettings: (SudokuViewModel.SudokuSettings) -> Unit,
) {

    val currentTheme = remember(settings.theme, settings) { settings.theme }
    val currentEffects = remember(settings.effects, settings) { settings.effects }

    var useSystemSetting by remember(currentTheme) { mutableStateOf(currentTheme.useSystem) }
    var isDarkSetting by remember(currentTheme) { mutableStateOf(currentTheme.isDark) }
    var isDynamicSetting by remember(currentTheme) { mutableStateOf(currentTheme.isDynamic) }

    var useHapticFeedback by remember(currentEffects) { mutableStateOf(currentEffects.useHaptic) }
    var useSoundEffects by remember(currentEffects) { mutableStateOf(currentEffects.useSounds) }
    var soundLevel by remember(currentEffects) { mutableFloatStateOf(currentEffects.soundVolume) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) }, navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            )
        }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(Dimens.Large), // Apply overall padding here
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.Large)
        ) {
            item(key = "Theme") {
                ElevatedCard {
                    Column(modifier = Modifier.padding(Dimens.Medium)) {
                        Text(
                            stringResource(R.string.theme_settings),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        HorizontalDivider()

                        SettingSwitcher(
                            R.string.use_system_default,
                            checked = useSystemSetting,
                        ) {
                            useSystemSetting = it
                        }

                        SettingSwitcher(
                            R.string.dark_theme,
                            checked = isDarkSetting,
                            enabled = !useSystemSetting,
                        ) {
                            isDarkSetting = it
                        }


                        SettingSwitcher(
                            R.string.dynamic_color_material_3,
                            checked = isDynamicSetting,
                        ) {
                            isDynamicSetting = it
                        }
                    }
                }
            }

            item(key = "Effects") {
                ElevatedCard {
                    Column(modifier = Modifier.padding(Dimens.Medium)) {
                        Text(
                            stringResource(R.string.effects_settings),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        HorizontalDivider()

                        SettingSwitcher(
                            R.string.use_haptic,
                            checked = useHapticFeedback,
                        ) { useHapticFeedback = it }

                        SettingSwitcher(
                            R.string.use_sound,
                            checked = useSoundEffects,
                        ) { useSoundEffects = it }
                    }
                }
            }

            item(key = "Store") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onSaveSettings(
                                SudokuViewModel.SudokuSettings(
                                    theme = SudokuBoardTheme(
                                        useSystem = useSystemSetting,
                                        isDark = isDarkSetting,
                                        isDynamic = isDynamicSetting
                                    ), effects = SudokuEffects(
                                        useHaptic = useHapticFeedback,
                                        useSounds = useSoundEffects,
                                        soundVolume = soundLevel
                                    )
                                )
                            )
                            onBackClick()
                        }) {
                        Icon(
                            Icons.Filled.TaskAlt,
                            contentDescription = stringResource(R.string.apply)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.apply))
                    }
                }
            }
        }
    }

}

@Composable
private fun SettingSwitcher(
    @StringRes title: Int,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (isChecked: Boolean) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(title))
        Switch(
            checked = checked, onCheckedChange = onCheckedChange, enabled = enabled
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSudokuSettingsScreen() {
    val dummySettings = SudokuViewModel.SudokuSettings(
        theme = SudokuBoardTheme(useSystem = false, isDark = true, isDynamic = true),
        effects = SudokuEffects(useHaptic = true, useSounds = true, soundVolume = 10f)
    )

    SudokuSettingsScreen(
        settings = dummySettings,
        onBackClick = { },
        onSaveSettings = { },
    )
}