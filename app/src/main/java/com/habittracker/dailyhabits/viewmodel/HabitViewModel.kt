package com.habittracker.dailyhabits.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.dailyhabits.database.HabitDao
import com.habittracker.dailyhabits.model.Habit
import kotlinx.coroutines.launch

class HabitViewModel(private val habitDao: HabitDao) : ViewModel() {

    private val _allHabits = MutableStateFlow<List<Habit>>(emptyList())
    val allHabits: StateFlow<List<Habit>> = _allHabits

    init {
        // Обновление данных в StateFlow
        viewModelScope.launch {
            habitDao.getAllHabits().collect { habits ->
                _allHabits.value = habits
            }
        }
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            habitDao.insertHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitDao.deleteHabit(habit)
        }
    }

    // Новый метод для обновления привычки
    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            habitDao.updateHabit(habit)
        }
    }

    fun updateHabitStatus(habit: Habit, date: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            val updatedDailyStatus = habit.dailyStatus.toMutableMap().apply {
                this[date] = isCompleted
            }
            val updatedHabit = habit.copy(dailyStatus = updatedDailyStatus)
            habitDao.updateHabit(updatedHabit)
        }
    }

    // Метод для получения привычки по ID
    suspend fun getHabitById(habitId: Int): Habit? {
        return habitDao.getHabitById(habitId)
    }
}