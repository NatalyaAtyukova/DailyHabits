package com.habittracker.dailyhabits.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.habittracker.dailyhabits.database.HabitDatabase
import com.habittracker.dailyhabits.gui.screen.AddHabitScreen
import com.habittracker.dailyhabits.gui.screen.EditHabitScreen
import com.habittracker.dailyhabits.gui.screen.HabitListScreen
import com.habittracker.dailyhabits.gui.screen.HabitStatsScreen
import com.habittracker.dailyhabits.services.ReminderManager
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import com.habittracker.dailyhabits.viewmodel.HabitViewModelFactory
import com.habittracker.dailyhabits.ui.components.InterstitialAdManager

@Composable
fun Navigation(database: HabitDatabase, interstitialAdManager: InterstitialAdManager) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val habitViewModel: HabitViewModel = viewModel(
        factory = HabitViewModelFactory(database.habitDao(), ReminderManager(context))
    )

    NavHost(navController = navController, startDestination = Screen.HabitList.route) {
        composable(Screen.HabitList.route) {
            HabitListScreen(
                viewModel = habitViewModel,
                onAddHabit = { navController.navigate(Screen.AddHabit.route) },
                onEditHabit = { habitId ->
                    navController.navigate("${Screen.EditHabit.route}/$habitId")
                },
                onOpenStats = { navController.navigate(Screen.HabitStats.route) },
                navController = navController,
                interstitialAdManager = interstitialAdManager
            )
        }
        composable(Screen.AddHabit.route) {
            AddHabitScreen(
                viewModel = habitViewModel,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "${Screen.EditHabit.route}/{habitId}",
            arguments = listOf(navArgument("habitId") { type = NavType.IntType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getInt("habitId")
            if (habitId != null) {
                EditHabitScreen(
                    habitViewModel = habitViewModel,
                    navController = navController,
                    habitId = habitId,
                    interstitialAdManager = interstitialAdManager
                )
            }
        }
        composable(Screen.HabitStats.route) {
            HabitStatsScreen(
                habitViewModel = habitViewModel,
                onBack = { navController.popBackStack() },
                interstitialAdManager = interstitialAdManager
            )
        }
    }
}

sealed class Screen(val route: String) {
    object HabitList : Screen("habitList")
    object AddHabit : Screen("addHabit")
    object EditHabit : Screen("editHabit")
    object HabitStats : Screen("habitStats")
}