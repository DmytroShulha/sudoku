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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var useSystemSetting by remember(settings.theme) { mutableStateOf(settings.theme.useSystem) }
    var isDarkSetting by remember(settings.theme) { mutableStateOf(settings.theme.isDark) }
    var isDynamicSetting by remember(settings.theme) { mutableStateOf(settings.theme.isDynamic) }

    var useHapticFeedback by remember(settings.effects) { mutableStateOf(settings.effects.useHaptic) }
    var useSoundEffects by remember(settings.effects) { mutableStateOf(settings.effects.useSounds) }

    val hasUnsavedChanges = remember(
        useSystemSetting, isDarkSetting, isDynamicSetting,
        useHapticFeedback, useSoundEffects,
        settings
    ) {
        useSystemSetting != settings.theme.useSystem ||
                isDarkSetting != settings.theme.isDark ||
                isDynamicSetting != settings.theme.isDynamic ||
                useHapticFeedback != settings.effects.useHaptic ||
                useSoundEffects != settings.effects.useSounds
    }

    Scaffold(
        topBar = {
            SettingsToolBar(onBackClick)
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
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

            itemButtons(
                hasUnsavedChanges = hasUnsavedChanges,
                onApply = {
                    onSaveSettings(
                        SudokuViewModel.SudokuSettings(
                            theme = SudokuBoardTheme(
                                useSystem = useSystemSetting,
                                isDark = isDarkSetting,
                                isDynamic = isDynamicSetting
                            ),
                            effects = SudokuEffects(
                                useHaptic = useHapticFeedback,
                                useSounds = useSoundEffects,
                            )
                        )
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Settings saved!"
                        )
                    }
                    onBackClick()
                }
            )
        }
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsToolBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

private fun LazyListScope.itemButtons(
    hasUnsavedChanges: Boolean,
    onApply: () -> Unit
) {
    item(key = "Store") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onApply,
                enabled = hasUnsavedChanges
            ) {
                Icon(
                    Icons.Filled.TaskAlt,
                    contentDescription = stringResource(R.string.apply)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    if (hasUnsavedChanges) {
                        stringResource(R.string.apply)
                    } else {
                        stringResource(R.string.no_changes)
                    }
                )
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
            Column(
                modifier = Modifier.padding(Dimens.Large),
                verticalArrangement = Arrangement.spacedBy(Dimens.Medium)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Medium)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        stringResource(R.string.effects_settings),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                SettingSwitcherWithDescription(
                    titleRes = R.string.use_haptic,
                    descriptionRes = R.string.use_haptic_desc,
                    icon = Icons.Filled.Vibration,
                    checked = useHapticFeedback,
                    onCheckedChange = onUseHapticFeedback
                )

                SettingSwitcherWithDescription(
                    titleRes = R.string.use_sound,
                    descriptionRes = R.string.use_sound_desc,
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
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
            Column(
                modifier = Modifier.padding(Dimens.Large),
                verticalArrangement = Arrangement.spacedBy(Dimens.Medium)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Medium)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        stringResource(R.string.theme_settings),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                SettingSwitcherWithDescription(
                    titleRes = R.string.use_system_default,
                    descriptionRes = R.string.use_system_default_desc,
                    icon = Icons.Filled.PhoneAndroid,
                    checked = params.useSystemSetting,
                    onCheckedChange = params.onUseSystemSetting
                )

                SettingSwitcherWithDescription(
                    titleRes = R.string.dark_theme,
                    descriptionRes = R.string.dark_theme_desc,
                    icon = Icons.Filled.DarkMode,
                    checked = params.isDarkSetting,
                    enabled = !params.useSystemSetting,
                    onCheckedChange = params.onIsDarkSetting
                )

                SettingSwitcherWithDescription(
                    titleRes = R.string.dynamic_color_material_3,
                    descriptionRes = R.string.dynamic_color_desc,
                    icon = Icons.Filled.Palette,
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
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun SettingSwitcherWithDescription(
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    icon: ImageVector,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (isChecked: Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Medium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.Small)
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
                Text(
                    text = stringResource(descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.width(Dimens.Medium))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
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
