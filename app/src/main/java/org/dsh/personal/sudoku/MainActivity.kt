package org.dsh.personal.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import org.dsh.personal.sudoku.core.Navigator
import org.dsh.personal.sudoku.core.rememberNavigationState
import org.dsh.personal.sudoku.core.toEntries
import org.dsh.personal.sudoku.presentation.SudokuFeatureEntry
import org.dsh.personal.sudoku.presentation.success.SuccessViewModel
import org.dsh.personal.sudoku.theme.PersonalTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            PersonalTheme {
                MainAppNavigation(windowSizeClass)
            }
        }
    }
}

@Composable
fun MainAppNavigation(windowSizeClass: WindowSizeClass) {

    val navigationState = rememberNavigationState(
        startRoute = SudokuRoutes.MainMenu,
        topLevelRoutes = setOf(SudokuRoutes.MainMenu)
    )

    val navigator = remember { Navigator(navigationState) }

    val entryProvider = entryProvider {
        entry<SudokuRoutes.MainMenu> { SudokuFeatureEntry.SudokuMainMenuScreen(navigator = navigator) }
        entry<SudokuRoutes.About> { SudokuFeatureEntry.SudokuAbout(navigator = navigator) }
        entry<SudokuRoutes.Settings> { SudokuFeatureEntry.SudokuSettings(navigator = navigator) }
        entry<SudokuRoutes.Statistic> { SudokuFeatureEntry.SudokuGameStatistic(navigator = navigator) }
        entry<SudokuRoutes.Success> {
            val viewModel = koinViewModel<SuccessViewModel>()
            val gameState by viewModel.gameState.collectAsState()
            SudokuFeatureEntry.SudokuSuccessGame(navigator = navigator, gameState)
        }
        entry<SudokuRoutes.GameScreen> { key ->
            val difficultyString = key.difficulty
            SudokuFeatureEntry.SudokuGameScreen(
                navigator = navigator,
                windowSizeClass = windowSizeClass,
                difficultyString = difficultyString
            )
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
        sceneStrategy = remember { DialogSceneStrategy() }
    )
}

