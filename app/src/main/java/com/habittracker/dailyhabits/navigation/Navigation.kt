package com.habittracker.dailyhabits.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.habittracker.dailyhabits.gui.screen.HabitListScreen
import com.habittracker.dailyhabits.gui.screen.AddHabitScreen
import com.habittracker.dailyhabits.viewmodel.HabitViewModel

@Composable
fun AppNavigation(viewModel: HabitViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "habit_list") {
        composable("habit_list") {
            HabitListScreen(
                viewModel = viewModel,
                onAddHabit = { navController.navigate("add_habit") }
            )
        }
        composable("add_habit") {
            AddHabitScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}