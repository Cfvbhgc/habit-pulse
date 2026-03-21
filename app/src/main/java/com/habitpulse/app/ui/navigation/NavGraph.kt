package com.habitpulse.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.ui.screens.addhabit.AddHabitScreen
import com.habitpulse.app.ui.screens.detail.HabitDetailScreen
import com.habitpulse.app.ui.screens.home.HomeScreen
import com.habitpulse.app.ui.screens.stats.StatsScreen

/**
 * Defines the navigation graph for the HabitPulse application.
 *
 * Sets up all composable destinations and their route mappings using Jetpack Navigation Compose.
 * Each screen receives the [navController] for triggering navigation events and the shared
 * [repository] for constructing ViewModels.
 *
 * @param navController The [NavHostController] that manages back stack and navigation state.
 * @param repository The shared [HabitRepository] instance passed to each screen's ViewModel.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    repository: HabitRepository
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                repository = repository,
                onAddHabit = {
                    navController.navigate(Screen.AddHabit.route)
                },
                onHabitClick = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                }
            )
        }

        composable(route = Screen.AddHabit.route) {
            AddHabitScreen(
                repository = repository,
                habitId = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditHabit.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: return@composable
            AddHabitScreen(
                repository = repository,
                habitId = habitId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: return@composable
            HabitDetailScreen(
                repository = repository,
                habitId = habitId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditHabit = {
                    navController.navigate(Screen.EditHabit.createRoute(habitId))
                },
                onViewStats = {
                    navController.navigate(Screen.Stats.createRoute(habitId))
                }
            )
        }

        composable(
            route = Screen.Stats.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: return@composable
            StatsScreen(
                repository = repository,
                habitId = habitId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
