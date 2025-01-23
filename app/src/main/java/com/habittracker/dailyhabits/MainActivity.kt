package com.habittracker.dailyhabits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.dailyhabits.database.HabitDatabase
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import com.habittracker.dailyhabits.viewmodel.HabitViewModelFactory
import com.habittracker.dailyhabits.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получите экземпляр HabitDao из базы данных Room
        val habitDao = HabitDatabase.getDatabase(applicationContext).habitDao()

        setContent {
            // Используйте фабрику для создания ViewModel
            val viewModel: HabitViewModel = viewModel(
                factory = HabitViewModelFactory(habitDao)
            )
            AppNavigation(viewModel = viewModel)
        }
    }
}