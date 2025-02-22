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

    fun editHabit(habit: Habit) {
        _selectedHabit.value = habit
    }

    suspend fun getHabitById(habitId: Int): Habit? {
        return habitDao.getHabitById(habitId)
    }

    fun updateHabitStatus(habit: Habit, date: Long, isCompleted: Boolean?) {
        viewModelScope.launch {
            val normalizedDate = getStartOfDay(date)
            val updatedDailyStatus = habit.dailyStatus.toMutableMap().apply {
                if (isCompleted == null) remove(normalizedDate) else put(normalizedDate, isCompleted)
            }.toMap()

            habitDao.updateHabit(habit.copy(dailyStatus = updatedDailyStatus))
        }
    }

    fun calculateProgress(habit: Habit): Triple<Float, Int, Int> {
        val totalDays = habit.deadline?.let {
            val calculatedDays = TimeUnit.MILLISECONDS.toDays(it - habit.timestamp).toInt()
            max(1, calculatedDays)
        } ?: 1

        val completedDays = habit.dailyStatus.count { it.value }
        val skippedDays = max(0, totalDays - completedDays)

        var maxStreak = 0
        var currentStreak = 0
        habit.dailyStatus.toSortedMap().forEach { (_, completed) ->
            if (completed) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 0
            }
        }

        val progress = (completedDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
        return Triple(progress, skippedDays, maxStreak)
    }

    private fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getStartOfDay(date: Long): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}