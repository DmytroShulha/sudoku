package org.dsh.personal.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import org.dsh.personal.sudoku.core.AppRoutes
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
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoutes.SUDOKU_FEATURE) {
        // Define other app-level destinations here
        composable("main_app_home") {
            MainMenuScreen(onSudokuClick = { navController.navigate(AppRoutes.SUDOKU_FEATURE) })
        }


        navigation(startDestination = SudokuRoutes.MAIN_MENU, route = AppRoutes.SUDOKU_FEATURE) {
            composable(SudokuRoutes.MAIN_MENU) {
                SudokuFeatureEntry.SudokuMainMenuScreen(navController = navController)
            }
            composable(SudokuRoutes.ABOUT_SCREEN) {
                SudokuFeatureEntry.SudokuAbout(navController = navController)
            }
            composable(SudokuRoutes.STATISTIC_SCREEN) {
                SudokuFeatureEntry.SudokuGameStatistic(navController = navController)
            }
            composable(SudokuRoutes.SETTINGS_SCREEN) {
                SudokuFeatureEntry.SudokuSettings(navController = navController)
            }
            composable(SudokuRoutes.SUCCESS_SCREEN) {
                val viewModel = koinViewModel<SuccessViewModel>()
                val gameState by viewModel.gameState.collectAsState()
                SudokuFeatureEntry.SudokuSuccessGame(navController = navController, gameState)
            }
            composable(
                route = SudokuRoutes.GAME_SCREEN,
                arguments = listOf(navArgument("difficulty") { type = NavType.StringType })
            ) { backStackEntry ->
                val difficultyString = backStackEntry.arguments?.getString("difficulty")
                SudokuFeatureEntry.SudokuGameScreen(
                    navController = navController,
                    windowSizeClass = windowSizeClass,
                    difficultyString = difficultyString
                )
            }
        }
    }
}

@Composable
fun MainMenuScreen(onSudokuClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onSudokuClick,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp) // Add padding to the button
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between text and icon
            ) {
                Text("Sudoku")
                Icon(
                    imageVector = Icons.Filled.Star, // Replace with your icon
                    contentDescription = "Sudoku Icon",
                    modifier = Modifier.size(24.dp) // Adjust icon size
                )
            }
        }
    }
}
