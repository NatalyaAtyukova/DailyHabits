package com.habittracker.dailyhabits.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.habittracker.dailyhabits.database.HabitDao
import com.habittracker.dailyhabits.services.ReminderManager

class HabitViewModelFactory(
    private val habitDao: HabitDao,
    private val reminderManager: ReminderManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(habitDao, reminderManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}