package com.habittracker.dailyhabits.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.dailyhabits.database.HabitDao
import com.habittracker.dailyhabits.model.Habit
import com.habittracker.dailyhabits.services.ReminderManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.habittracker.dailyhabits.model.HabitStats
import com.habittracker.dailyhabits.gui.screen.StatsPeriod
import java.util.*
import kotlin.math.roundToInt

class HabitViewModel(
    private val habitDao: HabitDao,
    private val reminderManager: ReminderManager
) : ViewModel() {

    private val _allHabits = MutableStateFlow<List<Habit>>(emptyList())
    val allHabits: StateFlow<List<Habit>> = _allHabits

    private val _filteredHabits = MutableStateFlow<List<Habit>>(emptyList())
    val filteredHabits: StateFlow<List<Habit>> = _filteredHabits

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag

    private val _habitStats = MutableStateFlow(HabitStats())
    val habitStats: StateFlow<HabitStats> = _habitStats

    private val habitStatsCache = mutableMapOf<Int, HabitStats>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            habitDao.getAllHabits().collect { habits ->
                _allHabits.value = habits
                _filteredHabits.value = habits
                _tags.value = habits.flatMap { it.tags }.distinct()
            }
        }
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = tag
        filterHabitsByTag(tag)
    }

    fun filterHabitsByTag(tag: String?) {
        if (tag == null) {
            _filteredHabits.value = _allHabits.value
        } else {
            _filteredHabits.value = _allHabits.value.filter { it.tags.contains(tag) }
        }
    }

    fun getHabitById(id: Int): Flow<Habit?> {
        return habitDao.getHabitById(id)
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch(Dispatchers.IO) {
            val newHabit = habit.copy(
                timestamp = System.currentTimeMillis(),
                dailyStatus = emptyMap()
            )
            val newId = habitDao.insertHabit(newHabit)
            reminderManager.scheduleReminder(newHabit.copy(id = newId.toInt()))
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch(Dispatchers.IO) {
            habitDao.deleteHabit(habit)
            reminderManager.cancelReminder(habit.id)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch(Dispatchers.IO) {
            habitDao.updateHabit(habit)
            reminderManager.scheduleReminder(habit)
        }
    }

    fun updateHabitStatus(habit: Habit, date: Long, value: Float?) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedStatus = habit.dailyStatus.toMutableMap()
            if (value == null) {
                updatedStatus.remove(date)
            } else {
                updatedStatus[date] = value
            }
            habitDao.updateHabit(habit.copy(dailyStatus = updatedStatus))
        }
    }

    fun calculateHabitStats(habits: List<Habit>, period: StatsPeriod = StatsPeriod.WEEK) {
        viewModelScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val endDate = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val startDate = if (period == StatsPeriod.ALL) {
                habits.minOfOrNull { it.timestamp } ?: endDate
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, -period.days)
                calendar.timeInMillis
            }

            var totalCompleted = 0
            var totalMissed = 0
            var maxStreak = 0
            var totalDaysInRange = 0

            habitStatsCache.clear()

            habits.forEach { habit ->
                val stats = calculateSingleHabitStats(habit, startDate, endDate)
                habitStatsCache[habit.id] = stats

                totalCompleted += stats.completedDays
                totalMissed += stats.missedDays
                maxStreak = maxOf(maxStreak, stats.longestStreak)
                totalDaysInRange += stats.totalDays
            }

            val averageCompletion = if (totalDaysInRange > 0) {
                (totalCompleted.toFloat() / totalDaysInRange * 100)
            } else 0f

            _habitStats.value = HabitStats(
                averageCompletion = averageCompletion,
                longestStreak = maxStreak,
                missedDays = totalMissed,
                totalHabits = habits.size,
                completedDays = totalCompleted,
                totalDays = totalDaysInRange
            )
        }
    }

    fun getHabitStats(habit: Habit): HabitStats? {
        return habitStatsCache[habit.id]
    }

    private fun calculateSingleHabitStats(
        habit: Habit,
        startDate: Long,
        endDate: Long
    ): HabitStats {
        var completed = 0
        var missed = 0
        var currentStreak = 0
        var maxStreak = 0
        var totalDays = 0

        val calendar = Calendar.getInstance()

        val habitCreationDate = calendar.apply {
            timeInMillis = habit.timestamp
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val calculationStartDate = maxOf(startDate, habitCreationDate)

        if (calculationStartDate > endDate) {
            return HabitStats()
        }

        calendar.timeInMillis = calculationStartDate

        val endCalendar = Calendar.getInstance().apply { timeInMillis = endDate }

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        while (calendar.timeInMillis <= endCalendar.timeInMillis) {
            val dayStart = calendar.timeInMillis
            totalDays++

            val status = habit.dailyStatus[dayStart]
            if (status != null && status >= (habit.targetValue ?: 1f)) {
                completed++
                currentStreak++
            } else {
                currentStreak = 0
                if (dayStart < todayStart) {
                    missed++
                }
            }
            maxStreak = maxOf(maxStreak, currentStreak)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return HabitStats(
            averageCompletion = if (totalDays > 0) (completed.toFloat() / totalDays * 100) else 0f,
            longestStreak = maxStreak,
            missedDays = missed,
            completedDays = completed,
            totalDays = totalDays
        )
    }
}