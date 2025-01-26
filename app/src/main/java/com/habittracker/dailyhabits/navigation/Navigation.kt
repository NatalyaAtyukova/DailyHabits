package com.habittracker.dailyhabits.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.habittracker.dailyhabits.gui.screen.HabitListScreen
import com.habittracker.dailyhabits.gui.screen.AddHabitScreen
import com.habittracker.dailyhabits.gui.screen.EditHabitScreen
import com.habittracker.dailyhabits.viewmodel.HabitViewModel

object Routes {
    const val HabitList = "habit_list"
    const val AddHabit = "add_habit"
    const val EditHabit = "edit_habit/{habitId}"
}

@Composable
fun AppNavigation(viewModel: HabitViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Routes.HabitList) {
        // Экран списка привычек
        composable(Routes.HabitList) {
            HabitListScreen(
                viewModel = viewModel,
                onAddHabit = { navController.navigate(Routes.AddHabit) },
                onEditHabit = { habit ->
                    navController.navigate("edit_habit/${habit.id}")
                }
            )
        }

        // Экран добавления привычки
        composable(Routes.AddHabit) {
            AddHabitScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Экран редактирования привычки
        composable(
            route = Routes.EditHabit,
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getInt("habitId")
            if (habitId != null) {
                EditHabitScreen(
                    viewModel = viewModel,
                    habitId = habitId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}