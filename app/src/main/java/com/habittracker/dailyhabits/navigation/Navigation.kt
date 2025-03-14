package com.habittracker.dailyhabits.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.habittracker.dailyhabits.ui.components.InterstitialAdManager
import com.habittracker.dailyhabits.gui.screen.AddHabitScreen
import com.habittracker.dailyhabits.gui.screen.EditHabitScreen
import com.habittracker.dailyhabits.gui.screen.HabitListScreen
import com.habittracker.dailyhabits.gui.screen.HabitStatsScreen
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import com.habittracker.dailyhabits.viewmodel.HabitStatsViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: HabitViewModel,
    statsViewModel: HabitStatsViewModel
) {
    val context = LocalContext.current
    val interstitialAdManager = remember { InterstitialAdManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            interstitialAdManager.destroy()
        }
    }

    NavHost(navController = navController, startDestination = "habitList") {
        composable("habitList") {
            HabitListScreen(
                viewModel = viewModel,
                onAddHabit = {
                    interstitialAdManager.showAd(context as Activity) {
                        navController.navigate("addHabit")
                    }
                },
                onEditHabit = { habit ->
                    interstitialAdManager.showAd(context as Activity) {
                        navController.navigate("editHabit/${habit.id}")
                    }
                },
                onOpenStats = {
                    interstitialAdManager.showAd(context as Activity) {
                        navController.navigate("habitStats")
                    }
                }
            )
        }

        composable("addHabit") {
            AddHabitScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("editHabit/{habitId}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")?.toIntOrNull() ?: return@composable
            EditHabitScreen(
                habitId = habitId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("habitStats") {
            HabitStatsScreen(
                viewModel = statsViewModel,
                habits = viewModel.allHabits.value,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}