package com.habittracker.dailyhabits.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.habittracker.dailyhabits.gui.screen.HabitListScreen
import com.habittracker.dailyhabits.gui.screen.AddHabitScreen
import com.habittracker.dailyhabits.gui.screen.EditHabitScreen
import com.habittracker.dailyhabits.gui.screen.HabitStatsScreen
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import com.habittracker.dailyhabits.viewmodel.HabitStatsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

object Routes {
    const val HabitList = "habit_list"
    const val AddHabit = "add_habit"
    const val EditHabit = "edit_habit/{habitId}"
    const val Stats = "habit_stats" // ✅ Добавлен маршрут для статистики
}

@Composable
fun AppNavigation(viewModel: HabitViewModel, statsViewModel: HabitStatsViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Routes.HabitList) {
        // Экран списка привычек
        composable(Routes.HabitList) {
            HabitListScreen(
                viewModel = viewModel,
                onAddHabit = { navController.navigate(Routes.AddHabit) },
                onEditHabit = { habit ->
                    navController.navigate("edit_habit/${habit.id}")
                },
                onOpenStats = { navController.navigate(Routes.Stats) } // ✅ Добавлен переход к статистике
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

        composable(Routes.Stats) {
            val habitsState = viewModel.allHabits.collectAsStateWithLifecycle(initialValue = emptyList()) // ✅ Исправлено
            val habits = habitsState.value
            HabitStatsScreen(viewModel = statsViewModel, habits = habits)
        }
    }
}