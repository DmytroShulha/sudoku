package org.dsh.personal.sudoku.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.twotone.Palette
import androidx.compose.material.icons.twotone.Pause
import androidx.compose.material.icons.twotone.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.SudokuRoutes
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.presentation.about.AboutScreen
import org.dsh.personal.sudoku.presentation.game.SudokuGame
import org.dsh.personal.sudoku.presentation.main.SudokuMainMenu
import org.dsh.personal.sudoku.presentation.settings.SudokuSettingsScreen
import org.dsh.personal.sudoku.presentation.statistic.SudokuAnalyticsScreen
import org.dsh.personal.sudoku.presentation.success.SuccessScreen
import org.dsh.personal.sudoku.presentation.view.ThemeSettingsDialog
import org.dsh.personal.sudoku.theme.PersonalTheme
import org.koin.androidx.compose.koinViewModel
import java.util.Locale
import kotlin.time.Duration
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.presentation.about.AboutViewModel
import org.dsh.personal.sudoku.presentation.main.DifficultySelectionSheet
import org.dsh.personal.sudoku.presentation.statistic.StatisticViewModel
import org.dsh.personal.sudoku.presentation.view.ErrorState
import org.dsh.personal.sudoku.presentation.view.LoadingState

object SudokuFeatureEntry {
    @Composable
    fun SudokuMainMenuScreen(navController: NavController) {
        val viewModel: SudokuViewModel = koinViewModel()
        val settings by viewModel.sudokuSettings.collectAsStateWithLifecycle()
        SudokuMainMenu(
            hasContinueGame = settings.hasContinueGame,
            onStartGame = { difficulty ->
                navController.navigate(SudokuRoutes.gameScreenRoute(difficulty.name))
            },
            onResumeGame = { navController.navigate(SudokuRoutes.gameScreenRoute(SudokuRoutes.PARAM_CONTINUE)) },
            onAboutClick = { navController.navigate(SudokuRoutes.ABOUT_SCREEN) },
            onSettingsClick = { navController.navigate(SudokuRoutes.SETTINGS_SCREEN) },
            onStatisticClick = { navController.navigate(SudokuRoutes.STATISTIC_SCREEN) })
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SudokuGameScreen(
        navController: NavController,
        difficultyString: String?,
        windowSizeClass: WindowSizeClass,
    ) {
        val viewModel: SudokuViewModel = koinViewModel()

        LaunchedEffect(Unit) {
            if (difficultyString == SudokuRoutes.PARAM_CONTINUE) {
                viewModel.resumeGame()
            } else {
                val difficulty = difficultyString?.let { Difficulty.valueOf(it) } ?: Difficulty.EASY
                viewModel.startNewGame(difficulty)
            }
        }

        val settings = viewModel.sudokuSettings.collectAsState().value
        val isDark = if (settings.theme.useSystem) isSystemInDarkTheme() else settings.theme.isDark
        val useMaterial3Colors = settings.theme.isDynamic

        var showThemeDialog by remember { mutableStateOf(false) }
        val gameState by viewModel.gameState.collectAsState()
        LaunchedEffect(gameState.isSolved) {
            if (gameState.isSolved) {
                navController.popBackStack(
                    SudokuRoutes.MAIN_MENU, false
                )
                navController.navigate(SudokuRoutes.SUCCESS_SCREEN)
            }
        }

        val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> {
                        viewModel.pauseGameTimer()
                    }

                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        PersonalTheme(isDark, useMaterial3Colors) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                        Text(
                            stringResource(
                                R.string.level_is,
                                gameState.difficulty.toString().capitalizeFirstLetter()
                            )
                        )
                    }, actions = {
                        IconButton(onClick = {
                            if (settings.isPaused) {
                                viewModel.resumeGameTimer()
                            } else {
                                viewModel.pauseGameTimer()
                            }
                        }) {
                            Icon(
                                imageVector = if (settings.isPaused) {
                                    Icons.TwoTone.PlayArrow
                                } else {
                                    Icons.TwoTone.Pause
                                }, contentDescription = stringResource(R.string.pause_game)
                            )
                        }

                        Text(settings.duration.toFormat())

                        IconButton(onClick = { showThemeDialog = true }) {
                            Icon(
                                imageVector = Icons.TwoTone.Palette,
                                contentDescription = stringResource(R.string.change_theme)
                            )
                        }
                    }, navigationIcon = {
                        IconButton(onClick = navController::popBackStack) {
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
                }) { padding ->

                SudokuGame(
                    windowSizeClass = windowSizeClass,
                    modifier = Modifier.padding(padding),
                    gameState = gameState,
                    onCellClick = viewModel::selectCell,
                    onNumberClick = viewModel::inputNumber2,
                    undoClick = viewModel::undo,
                    notesClick = viewModel::toggleInputMode,
                    sudokuSettings = settings,
                    resumeGame = viewModel::resumeGameTimer
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

    private fun Duration.toFormat(): String {
        val minutes = inWholeMinutes
        val seconds = inWholeSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    @Composable
    fun SudokuAbout(navController: NavController) {
        val viewModel: AboutViewModel = koinViewModel()
        val uriHandler = LocalUriHandler.current
        val urlHowToPlay = stringResource(R.string.sudoku_how_to_play)
        val feedbackEmail = stringResource(R.string.sudoku_feedback_email)
        val mailTo = viewModel.buildFeedbackEmailUri(stringResource(R.string.app_name), viewModel.appVersionName, feedbackEmail)

        AboutScreen(
            appName = stringResource(R.string.app_name),
            onHowToPlayClick = { uriHandler.openUri(urlHowToPlay) },
            onNavigateBack = navController::popBackStack,
            appVersion = viewModel.appVersionName,
            mailTo = mailTo,
            openPrivacyPolicy = viewModel::onPrivacyPolicyClick,
            dismissPrivacyPolicyDialog = viewModel::onDismissPrivacyPolicyDialog,
            showPrivacyPolicyDialog = viewModel.showPrivacyPolicyDialog,
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
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true // Expands fully or hides
        )
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
                    ModalBottomSheet(
                        sheetState = sheetState, onDismissRequest = { showNewGame = false }) {
                        DifficultySelectionSheet(
                            difficulties = Difficulty.entries,
                            onDifficultySelected = { selectedDifficulty ->
                                scope.launch {
                                    sheetState.hide()
                                }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showNewGame = false
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
                                        showNewGame = false
                                    }
                                }
                            })
                    }
                }
            }
        }
    }

    @Composable
    fun SudokuGameStatistic(navController: NavController) {
        val viewModel: StatisticViewModel = koinViewModel()
        val state by viewModel.gameStat.collectAsStateWithLifecycle()

        when {
            state.isLoading -> LoadingState()
            !state.isLoading && state.gameStats != null -> {
                SudokuAnalyticsScreen(
                    stats = state.gameStats!!,
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