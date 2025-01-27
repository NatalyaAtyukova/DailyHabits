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

    fun calculateProgress(habit: Habit): Float {
        val currentDate = System.currentTimeMillis()

        // Рассчитываем общее количество дней между созданием привычки и её дедлайном
        val totalDays = habit.deadline?.let {
            (it - habit.timestamp) / (24 * 60 * 60 * 1000) + 1
        } ?: 1

        // Создаём список всех дат между началом привычки и сегодняшним днём
        val startDate = habit.timestamp
        val daysBetween = (0 until totalDays).map { startDate + it * 24 * 60 * 60 * 1000 }

        // Считаем количество выполненных дней
        val completedDays = daysBetween.count { date ->
            habit.dailyStatus[date] == true // Проверяем, выполнена ли привычка в конкретный день
        }

        return if (totalDays > 0) completedDays.toFloat() / totalDays else 0f
    }

    // Метод для получения привычки по ID
    suspend fun getHabitById(habitId: Int): Habit? {
        return habitDao.getHabitById(habitId)
    }
}