package com.habittracker.dailyhabits

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.dailyhabits.database.HabitDatabase
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import com.habittracker.dailyhabits.viewmodel.HabitViewModelFactory
import com.habittracker.dailyhabits.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем и запрашиваем разрешение на уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        // Получаем экземпляр HabitDao из базы данных Room
        val habitDao = HabitDatabase.getDatabase(applicationContext).habitDao()

        setContent {
            // Используем фабрику для создания ViewModel
            val viewModel: HabitViewModel = viewModel(
                factory = HabitViewModelFactory(habitDao)
            )
            AppNavigation(viewModel = viewModel)
        }
    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    // Пользователь отказался, можно показать уведомление или диалог
                }
            }.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}