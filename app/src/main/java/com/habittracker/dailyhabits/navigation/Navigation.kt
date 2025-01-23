package com.habittracker.dailyhabits.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import com.habittracker.dailyhabits.gui.screen.*
@Composable
fun AppNavigation(viewModel: HabitViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "habit_list") {
        composable("habit_list") {
            HabitListScreen(viewModel = viewModel)
        }
        composable("add_habit") {
            AddHabitScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}