package com.habittracker.dailyhabits.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.dailyhabits.database.HabitDao
import com.habittracker.dailyhabits.model.Habit
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class HabitViewModel(private val habitDao: HabitDao) : ViewModel() {

    private val _allHabits = MutableStateFlow<List<Habit>>(emptyList())
    val allHabits: StateFlow<List<Habit>> = _allHabits

    init {
        viewModelScope.launch {
            habitDao.getAllHabits().collect { habits ->
                _allHabits.value = habits
            }
        }
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            val today = getStartOfToday()
            habitDao.insertHabit(habit.copy(timestamp = today))
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitDao.deleteHabit(habit)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            habitDao.updateHabit(habit)
        }
    }

    suspend fun getHabitById(habitId: Int): Habit? {
        return habitDao.getHabitById(habitId)
    }

    fun updateHabitStatus(habit: Habit, date: Long, isCompleted: Boolean?) {
        viewModelScope.launch {
            val normalizedDate = getStartOfDay(date) // Теперь точно нормализуем день
            val updatedDailyStatus = habit.dailyStatus.toMutableMap().apply {
                if (isCompleted == null) remove(normalizedDate) else put(normalizedDate, isCompleted)
            }
            habitDao.updateHabit(habit.copy(dailyStatus = updatedDailyStatus))
        }
    }

    fun calculateProgress(habit: Habit): Float {
        val totalDays = habit.deadline?.let {
            val calculatedDays = TimeUnit.MILLISECONDS.toDays(it - habit.timestamp).toInt() + 1
            if (calculatedDays > 0) calculatedDays else 1
        } ?: 1

        val startDate = habit.timestamp
        val daysBetween =
            (0 until totalDays).map { startDate + TimeUnit.DAYS.toMillis(it.toLong()) }

        val completedDays = daysBetween.count { date ->
            habit.dailyStatus[date] == true
        }

        return if (totalDays > 0) completedDays.toFloat() / totalDays else 0f
    }

    private fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getStartOfDay(date: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}