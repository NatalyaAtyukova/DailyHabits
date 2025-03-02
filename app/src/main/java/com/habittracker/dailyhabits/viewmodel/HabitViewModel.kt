package com.habittracker.dailyhabits.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.dailyhabits.database.HabitDao
import com.habittracker.dailyhabits.model.Habit
import kotlinx.coroutines.launch
import kotlin.math.max
import java.util.*
import java.util.concurrent.TimeUnit

class HabitViewModel(private val habitDao: HabitDao) : ViewModel() {

    private val _allHabits = MutableStateFlow<List<Habit>>(emptyList())
    val allHabits: StateFlow<List<Habit>> = _allHabits

    private val _selectedHabit = MutableStateFlow<Habit?>(null)
    val selectedHabit: StateFlow<Habit?> = _selectedHabit

    init {
        viewModelScope.launch {
            habitDao.getAllHabits().collect { habits ->
                _allHabits.value = habits
                android.util.Log.d("HabitViewModel", "Habits updated: ${habits.map { it.id to it.dailyStatus }}")
            }
        }
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            val deviceTime = System.currentTimeMillis()
            val today = getStartOfToday(deviceTime)
            
            android.util.Log.d("HabitViewModel", "Adding new habit:")
            android.util.Log.d("HabitViewModel", "Device time: ${Date(deviceTime)}")
            android.util.Log.d("HabitViewModel", "Normalized today: ${Date(today)}")
            
            val newHabit = habit.copy(
                timestamp = today,
                dailyStatus = emptyMap()
            )
            habitDao.insertHabit(newHabit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitDao.deleteHabit(habit)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            val deviceTime = System.currentTimeMillis()
            android.util.Log.d("HabitViewModel", "Updating habit ${habit.id}:")
            android.util.Log.d("HabitViewModel", "Device time: ${Date(deviceTime)}")
            android.util.Log.d("HabitViewModel", "Original deadline: ${habit.deadline?.let { Date(it) }}")
            
            val updatedHabit = habit.copy(
                deadline = habit.deadline?.let { getStartOfDay(it) }
            )
            
            android.util.Log.d("HabitViewModel", "Normalized deadline: ${updatedHabit.deadline?.let { Date(it) }}")
            habitDao.updateHabit(updatedHabit)
        }
    }

    fun editHabit(habit: Habit) {
        _selectedHabit.value = habit
    }

    suspend fun getHabitById(habitId: Int): Habit? {
        return habitDao.getHabitById(habitId)
    }

    fun updateHabitStatus(habit: Habit, date: Long, isCompleted: Boolean?) {
        viewModelScope.launch {
            val normalizedDate = getStartOfDay(date)
            android.util.Log.d("HabitViewModel", "Updating status for date ${Date(normalizedDate)}: habitId=${habit.id}, isCompleted=$isCompleted")
            
            val updatedDailyStatus = habit.dailyStatus.toMutableMap().apply {
                if (isCompleted == null) {
                    remove(normalizedDate)
                } else {
                    put(normalizedDate, isCompleted)
                }
            }.toMap()
            
            val updatedHabit = habit.copy(dailyStatus = updatedDailyStatus)
            habitDao.updateHabit(updatedHabit)
            
            android.util.Log.d("HabitViewModel", "Status updated. Old status: ${habit.dailyStatus}")
            android.util.Log.d("HabitViewModel", "New status: $updatedDailyStatus")
        }
    }

    fun calculateProgress(habit: Habit): Triple<Float, Int, Int> {
        val deviceTime = System.currentTimeMillis()
        val now = getStartOfToday(deviceTime)
        val dayInMillis = 24 * 60 * 60 * 1000L

        // Определяем период для расчета прогресса
        val startDate = habit.timestamp
        val endDate = minOf(habit.deadline ?: now, now)

        var completedDays = 0
        var skippedDays = 0
        var currentStreak = 0
        var maxStreak = 0

        // Проходим по всем дням от начала до текущей даты
        var currentDate = startDate
        while (currentDate <= endDate) {
            val status = habit.dailyStatus[currentDate]
            val isPastDay = currentDate < (now - dayInMillis)

            when {
                status == true -> {
                    completedDays++
                    currentStreak++
                    maxStreak = maxOf(maxStreak, currentStreak)
                }
                status == false || (status == null && isPastDay) -> {
                    skippedDays++
                    currentStreak = 0
                }
            }
            currentDate += dayInMillis
        }

        // Считаем только прошедшие дни для прогресса
        val totalPassedDays = ((endDate - startDate) / dayInMillis).toInt() + 1
        
        android.util.Log.d("HabitViewModel", """Progress calculation:
            |habitId: ${habit.id}
            |name: ${habit.name}
            |startDate: ${Date(startDate)}
            |endDate: ${Date(endDate)}
            |now: ${Date(now)}
            |device time: ${Date(deviceTime)}
            |totalPassedDays: $totalPassedDays
            |completedDays: $completedDays
            |skippedDays: $skippedDays
            |maxStreak: $maxStreak
            |dailyStatus: ${habit.dailyStatus.map { (date, status) -> "${Date(date)}: $status" }}
        """.trimMargin())

        val progress = if (totalPassedDays > 0) {
            completedDays.toFloat() / totalPassedDays.toFloat()
        } else {
            0f
        }

        return Triple(progress.coerceIn(0f, 1f), skippedDays, maxStreak)
    }

    private fun getStartOfToday(deviceTime: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = deviceTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getStartOfDay(date: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}