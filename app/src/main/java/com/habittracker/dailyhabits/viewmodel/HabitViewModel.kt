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

    // ✅ Добавляем LiveData/StateFlow для хранения редактируемой привычки
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
        _selectedHabit.value = habit // ✅ Теперь переменная объявлена, и метод работает
    }

    suspend fun getHabitById(habitId: Int): Habit? {
        return habitDao.getHabitById(habitId)
    }

    fun updateHabitStatus(habit: Habit, date: Long, isCompleted: Boolean?) {
        viewModelScope.launch {
            val normalizedDate = getStartOfDay(date)

            // ✅ Глубокая копия мапы, чтобы избежать проблем с изменением
            val updatedDailyStatus = habit.dailyStatus.toMutableMap().apply {
                if (isCompleted == null) remove(normalizedDate) else put(normalizedDate, isCompleted)
            }.toMap() // ✅ Создаем новую неизменяемую мапу

            habitDao.updateHabit(habit.copy(dailyStatus = updatedDailyStatus))
        }
    }

    fun calculateProgress(habit: Habit): Pair<Float, Int> {
        val totalDays = habit.deadline?.let {
            val calculatedDays = TimeUnit.MILLISECONDS.toDays(it - habit.timestamp).toInt()
            max(1, calculatedDays) // Исключаем отрицательные значения
        } ?: 1

        val completedDays = habit.dailyStatus.count { it.value } // Количество выполненных дней

        // ✅ Исключаем отрицательные пропущенные дни
        val skippedDays = max(0, totalDays - completedDays)

        val progress = (completedDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f) // Ограничиваем от 0 до 1
        return Pair(progress, skippedDays) // Возвращаем и прогресс, и пропущенные дни
    }

    private fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { // ✅ Принудительно используем UTC
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getStartOfDay(date: Long): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { // ✅ Исправлено
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}