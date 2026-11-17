package com.nowlsantos.stockfishandme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nowlsantos.stockfishandme.domain.BoardTheme
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.presentation.GameViewModel
import com.nowlsantos.stockfishandme.ui.dialogs.ShowExitDialog
import com.nowlsantos.stockfishandme.ui.screens.ChessScreen
import com.nowlsantos.stockfishandme.ui.screens.SelectionScreen
import com.nowlsantos.stockfishandme.ui.theme.DeepDarkBackground
import com.nowlsantos.stockfishandme.ui.theme.StockfishAndMeTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up immersive mode
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var showExitDialog by remember { mutableStateOf(false) }

            BackHandler {
                showExitDialog = true
            }

            // Wrap everything inside KoinContext to bind Koin to the Compose tree
            StockfishAndMeTheme {
                MainScreen()

                if (showExitDialog) {
                    ShowExitDialog(
                        onDismiss = { showExitDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepDarkBackground,
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "selection",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("selection") {
                SelectionScreen(navController = navController)
            }
            composable(
                route = "chessScreen/{perspective}/{depth}/{themeName}",
                arguments = listOf(
                    navArgument("perspective") { type = NavType.StringType },
                    navArgument("depth") { type = NavType.IntType },
                    navArgument("themeName") { type = NavType.StringType },
                )
            ) { backStackEntry ->
                val viewModel: GameViewModel = koinViewModel()
                val perspectiveStr = backStackEntry.arguments?.getString("perspective") ?: PieceColor.WHITE.name
                val depth = backStackEntry.arguments?.getInt("depth") ?: 10
                val themeNameStr = backStackEntry.arguments?.getString("themeName") ?: BoardTheme.GREEN.name

                val perspective = try { PieceColor.valueOf(perspectiveStr) } catch (_: Exception) { PieceColor.WHITE }
                val themeName = try { BoardTheme.valueOf(themeNameStr) } catch (_: Exception) { BoardTheme.GREEN }

                ChessScreen(
                    perspective = perspective,
                    depth = depth,
                    boardTheme = themeName,
                    viewModel = viewModel
                )
            }
        }
    }
}
