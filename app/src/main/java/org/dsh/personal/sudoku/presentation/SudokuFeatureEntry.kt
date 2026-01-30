package org.dsh.personal.sudoku.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.SudokuRoutes
import org.dsh.personal.sudoku.core.Navigator
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.presentation.about.AboutScreen
import org.dsh.personal.sudoku.presentation.about.AboutScreenData
import org.dsh.personal.sudoku.presentation.about.AboutViewModel
import org.dsh.personal.sudoku.presentation.game.SudokuGame
import org.dsh.personal.sudoku.presentation.game.SudokuGameCallbacks
import org.dsh.personal.sudoku.presentation.game.SudokuGameSideEffects
import org.dsh.personal.sudoku.presentation.main.DifficultySelectionSheet
import org.dsh.personal.sudoku.presentation.main.GameToolBar
import org.dsh.personal.sudoku.presentation.main.SudokuMainMenu
import org.dsh.personal.sudoku.presentation.main.SudokuMainMenuData
import org.dsh.personal.sudoku.presentation.settings.SudokuSettingsScreen
import org.dsh.personal.sudoku.presentation.statistic.StatisticViewModel
import org.dsh.personal.sudoku.presentation.statistic.SudokuAnalyticsScreen
import org.dsh.personal.sudoku.presentation.success.SuccessScreen
import org.dsh.personal.sudoku.presentation.view.ErrorState
import org.dsh.personal.sudoku.presentation.view.LoadingState
import org.dsh.personal.sudoku.presentation.view.ThemeSettingsDialog
import org.dsh.personal.sudoku.theme.PersonalTheme
import org.koin.androidx.compose.koinViewModel


object SudokuFeatureEntry {
    @Composable
    fun SudokuMainMenuScreen(navigator: Navigator) {
        val viewModel: SudokuViewModel = koinViewModel()
        val settings by viewModel.sudokuSettings.collectAsStateWithLifecycle()
        SudokuMainMenu(
            data = SudokuMainMenuData(
                hasContinueGame = settings.hasContinueGame,
            onStartGame = { difficulty ->
                navigator.navigate(SudokuRoutes.GameScreen(difficulty.name))
            },
            onResumeGame = { navigator.navigate(SudokuRoutes.GameScreen(SudokuRoutes.PARAM_CONTINUE)) },
            onAboutClick = { navigator.navigate(SudokuRoutes.About) },
            onSettingsClick = { navigator.navigate(SudokuRoutes.Settings) },
            onStatisticClick = { navigator.navigate(SudokuRoutes.Statistic) }))
    }

    @Composable
    fun SudokuGameScreen(
        navigator: Navigator,
        difficultyString: String?,
        windowSizeClass: WindowSizeClass,
    ) {
        val viewModel: SudokuViewModel = koinViewModel()

        LaunchedEffect(Unit) {
            if (difficultyString == SudokuRoutes.PARAM_CONTINUE) {
                viewModel.handleIntent(SudokuViewModel.SudokuIntent.ResumeGame)
            } else {
                val difficulty = difficultyString?.let { Difficulty.valueOf(it) } ?: Difficulty.EASY
                viewModel.startNewGame(difficulty)
            }
        }

        val settings by viewModel.sudokuSettings.collectAsState()
        val isDark = if (settings.theme.useSystem) isSystemInDarkTheme() else settings.theme.isDark
        val useMaterial3Colors = settings.theme.isDynamic

        var showThemeDialog by remember { mutableStateOf(false) }
        val gameState by viewModel.gameState.collectAsState()
        SudokuGameSideEffects(gameState, navigator, viewModel)

        PersonalTheme(isDark, useMaterial3Colors) {
            Scaffold(
                topBar = {
                    GameToolBar(
                        gameState = gameState,
                        settings = settings,
                        popBack = { navigator.goBack() },
                        showThemeDialog = { showThemeDialog = true },
                        onPauseResumeClick = {
                            if (settings.isPaused) {
                                viewModel.handleIntent(SudokuViewModel.SudokuIntent.ResumeGameTimer)
                            } else {
                                viewModel.handleIntent(SudokuViewModel.SudokuIntent.PauseGameTimer)
                            }
                        })
                }) { padding ->

                SudokuGame(
                    modifier = Modifier.padding(padding),
                    windowSizeClass = windowSizeClass,
                    gameState = gameState,
                    sudokuSettings = settings,
                    callbacks = SudokuGameCallbacks(
                        onCellClick = viewModel::selectCell,
                        onNumberClick = viewModel::inputNumber2,
                        undoClick = viewModel::undo,
                        notesClick = viewModel::toggleInputMode,
                        resumeGame = { viewModel.handleIntent(SudokuViewModel.SudokuIntent.ResumeGameTimer) }))
            }

            if (showThemeDialog) {
                ThemeSettingsDialog(
                    currentTheme = settings.theme, // Pass the current theme state
                    onThemeChange = viewModel::updateAndSaveTheme,
                    onDismissRequest = { showThemeDialog = false } // Dismiss the dialog
                )
            }
        }
    }

    @Composable
    fun SudokuAbout(navigator: Navigator) {
        val viewModel: AboutViewModel = koinViewModel()
        val uriHandler = LocalUriHandler.current
        val urlHowToPlay = stringResource(R.string.sudoku_how_to_play)
        val githubRepo = stringResource(R.string.sudoku_git_repo_url)
        val playStoreGameUrl = stringResource(R.string.play_store_game_url)
        val playStoreUrl = stringResource(R.string.play_store_url)

        AboutScreen(
            onHowToPlayClick = { uriHandler.openUri(urlHowToPlay) },
            onNavigateBack = navigator::goBack,
            params = AboutScreenData(
                appName = stringResource(R.string.app_name),
                appVersion = viewModel.appVersionName,
                playStoreGameUrl = playStoreGameUrl,
                githubRepo = githubRepo,
                playStoreUrl = playStoreUrl
            )
        )
    }

    @Composable
    fun SudokuSettings(navigator: Navigator) {
        val viewModel = koinViewModel<SudokuViewModel>()
        val settings by viewModel.sudokuSettings.collectAsStateWithLifecycle()

        SudokuSettingsScreen(
            settings = settings,
            onBackClick = navigator::goBack,
            onSaveSettings = viewModel::saveSettings,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SudokuSuccessGame(navigator: Navigator, gameState: SudokuGameState) {

        var showNewGame by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()
        PersonalTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(), topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        title = { Text(stringResource(R.string.you_did_it)) },
                    )
                }) { padding ->
                SuccessScreen(
                    modifier = Modifier.padding(padding),
                    gameStats = gameState,
                    onNewGameClicked = {
                        showNewGame = true
                    },
                    onMainMenuClicked = {
                        navigator.goBack()
                        navigator.goBack()
                    },
                    onShareClicked = null,
                )

                if (showNewGame) {
                    NewGameModal(onDismiss = { showNewGame = false }, scope, navigator)
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun NewGameModal(
        onDismiss: () -> Unit, scope: CoroutineScope, navigator: Navigator
    ) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true // Expands fully or hides
        )
        ModalBottomSheet(
            sheetState = sheetState, onDismissRequest = onDismiss
        ) {
            DifficultySelectionSheet(
                difficulties = Difficulty.entries,
                onDifficultySelected = { selectedDifficulty ->
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismiss()
                            navigator.goBack()
                            navigator.goBack()
                            navigator.navigate(SudokuRoutes.GameScreen(selectedDifficulty.name))

                        }
                    }
                },
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismiss()
                        }
                    }
                })
        }
    }

    @Composable
    fun SudokuGameStatistic(navigator: Navigator) {
        val viewModel: StatisticViewModel = koinViewModel()
        val state by viewModel.gameStat.collectAsStateWithLifecycle()
        val gameState = state.gameStats
        when {
            state.isLoading -> LoadingState()
            !state.isLoading && gameState != null -> {
                SudokuAnalyticsScreen(
                    stats = gameState,
                    onNavigateBack = navigator::goBack,
                    onClearStat = viewModel::clearStat,
                )
            }

            else -> ErrorState(
                errorMessage = stringResource(R.string.error_loading_statistic), retry = false
            ) { }
        }
    }
}

fun String.capitalizeFirstLetter(): String {
    if (isEmpty()) {
        return this
    }
    return this[0].uppercaseChar() + this.substring(1).lowercase()
}
