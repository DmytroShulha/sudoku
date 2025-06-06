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
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.SudokuRoutes
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
    fun SudokuMainMenuScreen(navController: NavController) {
        val viewModel: SudokuViewModel = koinViewModel()
        val settings by viewModel.sudokuSettings.collectAsStateWithLifecycle()
        SudokuMainMenu(data = SudokuMainMenuData(
            hasContinueGame = settings.hasContinueGame,
            onStartGame = { difficulty ->
                navController.navigate(SudokuRoutes.gameScreenRoute(difficulty.name))
            },
            onResumeGame = { navController.navigate(SudokuRoutes.gameScreenRoute(SudokuRoutes.PARAM_CONTINUE)) },
            onAboutClick = { navController.navigate(SudokuRoutes.ABOUT_SCREEN) },
            onSettingsClick = { navController.navigate(SudokuRoutes.SETTINGS_SCREEN) },
            onStatisticClick = { navController.navigate(SudokuRoutes.STATISTIC_SCREEN) }
        ))
    }

    @Composable
    fun SudokuGameScreen(
        navController: NavController,
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
        SudokuGameSideEffects(gameState, navController, viewModel)

        PersonalTheme(isDark, useMaterial3Colors) {
            Scaffold(
                topBar = {
                    GameToolBar(
                        gameState = gameState,
                        settings = settings,
                        popBack = { navController.popBackStack() },
                        showThemeDialog = { showThemeDialog = true },
                        onPauseResumeClick = {
                            if (settings.isPaused) {
                                viewModel.handleIntent(SudokuViewModel.SudokuIntent.ResumeGameTimer)
                            } else {
                                viewModel.handleIntent(SudokuViewModel.SudokuIntent.PauseGameTimer)
                            }
                        }
                    )
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
                        resumeGame = { viewModel.handleIntent(SudokuViewModel.SudokuIntent.ResumeGameTimer) }
                    )
                )
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
    fun SudokuAbout(navController: NavController) {
        val viewModel: AboutViewModel = koinViewModel()
        val uriHandler = LocalUriHandler.current
        val urlHowToPlay = stringResource(R.string.sudoku_how_to_play)
        val githubRepo = stringResource(R.string.sudoku_git_repo_url)
        val playStoreGameUrl = stringResource(R.string.play_store_game_url)
        val playStoreUrl = stringResource(R.string.play_store_url)

        AboutScreen(
            onHowToPlayClick = { uriHandler.openUri(urlHowToPlay) },
            onNavigateBack = navController::popBackStack,
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
    fun SudokuSettings(navController: NavController) {
        val viewModel = koinViewModel<SudokuViewModel>()
        val settings by viewModel.sudokuSettings.collectAsStateWithLifecycle()

        SudokuSettingsScreen(
            settings = settings,
            onBackClick = navController::popBackStack,
            onSaveSettings = viewModel::saveSettings,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SudokuSuccessGame(navController: NavController, gameState: SudokuGameState) {

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
                        navController.popBackStack(
                            SudokuRoutes.MAIN_MENU, false
                        )
                    },
                    onShareClicked = null,
                )

                if (showNewGame) {
                    NewGameModal(onDismiss = { showNewGame = false }, scope, navController)
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun NewGameModal(
        onDismiss: () -> Unit,
        scope: CoroutineScope,
        navController: NavController
    ) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true // Expands fully or hides
        )
        ModalBottomSheet(
            sheetState = sheetState, onDismissRequest = onDismiss) {
            DifficultySelectionSheet(
                difficulties = Difficulty.entries,
                onDifficultySelected = { selectedDifficulty ->
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismiss()
                            navController.popBackStack(
                                SudokuRoutes.MAIN_MENU, false
                            )
                            navController.navigate(
                                SudokuRoutes.gameScreenRoute(
                                    selectedDifficulty.name
                                )
                            )
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
    fun SudokuGameStatistic(navController: NavController) {
        val viewModel: StatisticViewModel = koinViewModel()
        val state by viewModel.gameStat.collectAsStateWithLifecycle()
        val gameState = state.gameStats
        when {
            state.isLoading -> LoadingState()
            !state.isLoading && gameState != null -> {
                SudokuAnalyticsScreen(
                    stats = gameState,
                    onNavigateBack = navController::popBackStack,
                    onClearStat = viewModel::clearStat,
                )
            }
            else -> ErrorState(errorMessage = stringResource(R.string.error_loading_statistic), retry = false) { }
        }
    }
}

fun String.capitalizeFirstLetter(): String {
    if (isEmpty()) {
        return this
    }
    return this[0].uppercaseChar() + this.substring(1).lowercase()
}
