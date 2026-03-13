package com.takatagit.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.takatagit.app.ui.screen.LogDetailScreen
import com.takatagit.app.ui.screen.MainListScreen
import com.takatagit.app.ui.screen.SettingsScreen
import com.takatagit.app.ui.viewmodel.LogDetailViewModel
import com.takatagit.app.ui.viewmodel.MainListViewModel
import com.takatagit.app.ui.viewmodel.SettingsViewModel

@Composable
fun TakataGitApp(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Logs.route,
        modifier = modifier,
    ) {
        composable(route = AppDestination.Logs.route) {
            val viewModel: MainListViewModel = viewModel(factory = MainListViewModel.Factory)
            MainListScreen(
                viewModel = viewModel,
                onOpenSettings = {
                    navController.navigate(AppDestination.Settings.route)
                },
                onLogClick = { id ->
                    navController.navigate(AppDestination.LogDetail.createRoute(id))
                },
            )
        }

        composable(
            route = AppDestination.LogDetail.route,
            arguments = listOf(
                navArgument("logId") { type = NavType.StringType },
            ),
        ) {
            val viewModel: LogDetailViewModel = viewModel(factory = LogDetailViewModel.Factory)
            LogDetailScreen(
                viewModel = viewModel,
                onNavigateUp = navController::navigateUp,
            )
        }

        composable(route = AppDestination.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            SettingsScreen(
                viewModel = viewModel,
                onNavigateUp = navController::navigateUp,
            )
        }
    }
}

sealed class AppDestination(val route: String) {
    data object Logs : AppDestination("logs")
    data object Settings : AppDestination("settings")

    data object LogDetail : AppDestination("logs/{logId}") {
        fun createRoute(logId: String): String = "logs/$logId"
    }
}
