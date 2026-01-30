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
import androidx.compose.foundation.lazy.LazyListScope
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

    Scaffold(
        topBar = {
            SettingsToolBar(onBackClick)
        }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(Dimens.Large), // Apply overall padding here
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.Large)
        ) {
            itemTheme(
                ItemTheme(
                    useSystemSetting = useSystemSetting,
                    isDarkSetting = isDarkSetting,
                    isDynamicSetting = isDynamicSetting,
                    onUseSystemSetting = { useSystemSetting = it },
                    onIsDarkSetting = { isDarkSetting = it },
                    onIsDynamicSetting = { isDynamicSetting = it })
            )

            itemEffects(
                useHapticFeedback = useHapticFeedback,
                useSoundEffects = useSoundEffects,
                onUseHapticFeedback = { useHapticFeedback = it },
                onUseSoundEffects = { useSoundEffects = it })

            itemButtons {
                onSaveSettings(
                    SudokuViewModel.SudokuSettings(
                        theme = SudokuBoardTheme(
                            useSystem = useSystemSetting,
                            isDark = isDarkSetting,
                            isDynamic = isDynamicSetting
                        ), effects = SudokuEffects(
                            useHaptic = useHapticFeedback,
                            useSounds = useSoundEffects,
                        )
                    )
                )
                onBackClick()
            }
        }
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsToolBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.settings)) }, navigationIcon = {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    )
}

private fun LazyListScope.itemButtons(applyChanges: () -> Unit) {
    item(key = "Store") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = applyChanges
            ) {
                Icon(
                    Icons.Filled.TaskAlt, contentDescription = stringResource(R.string.apply)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.apply))
            }
        }
    }
}

private fun LazyListScope.itemEffects(
    useHapticFeedback: Boolean,
    useSoundEffects: Boolean,
    onUseHapticFeedback: (isChecked: Boolean) -> Unit,
    onUseSoundEffects: (isChecked: Boolean) -> Unit
) {
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
                    onCheckedChange = onUseHapticFeedback
                )

                SettingSwitcher(
                    R.string.use_sound,
                    checked = useSoundEffects,
                    onCheckedChange = onUseSoundEffects
                )
            }
        }
    }
}

data class ItemTheme(
    val useSystemSetting: Boolean,
    val isDarkSetting: Boolean,
    val isDynamicSetting: Boolean,
    val onUseSystemSetting: (isChecked: Boolean) -> Unit,
    val onIsDarkSetting: (isChecked: Boolean) -> Unit,
    val onIsDynamicSetting: (isChecked: Boolean) -> Unit
)

private fun LazyListScope.itemTheme(params: ItemTheme) {
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
                    checked = params.useSystemSetting,
                    onCheckedChange = params.onUseSystemSetting
                )

                SettingSwitcher(
                    R.string.dark_theme,
                    checked = params.isDarkSetting,
                    enabled = !params.useSystemSetting,
                    onCheckedChange = params.onIsDarkSetting
                )


                SettingSwitcher(
                    R.string.dynamic_color_material_3,
                    checked = params.isDynamicSetting,
                    onCheckedChange = params.onIsDynamicSetting
                )
            }
        }
    }
}

@Composable
fun SettingSwitcher(
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
        effects = SudokuEffects(useHaptic = true, useSounds = true)
    )

    SudokuSettingsScreen(
        settings = dummySettings,
        onBackClick = { },
        onSaveSettings = { },
    )
}
