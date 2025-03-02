package com.habittracker.dailyhabits.viewmodel

import androidx.lifecycle.ViewModel
import com.habittracker.dailyhabits.model.Habit
import com.habittracker.dailyhabits.model.HabitStats
import com.habittracker.dailyhabits.gui.screen.StatsPeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import kotlin.math.roundToInt

class HabitStatsViewModel : ViewModel() {
    private val _habitStats = MutableStateFlow(HabitStats())
    val habitStats: StateFlow<HabitStats> = _habitStats

    private val habitStatsCache = mutableMapOf<Int, HabitStats>()

    fun calculateHabitStats(habits: List<Habit>, period: StatsPeriod = StatsPeriod.WEEK) {
        val calendar = Calendar.getInstance()
        val endDate = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startDate = calendar.apply {
            add(Calendar.DAY_OF_YEAR, -period.days)
        }.timeInMillis

        var totalCompleted = 0
        var totalMissed = 0
        var maxStreak = 0
        var totalDays = 0

        habits.forEach { habit ->
            val stats = calculateHabitStats(habit, startDate, endDate)
            totalCompleted += stats.completedDays
            totalMissed += stats.missedDays
            maxStreak = maxOf(maxStreak, stats.longestStreak)
            totalDays += stats.totalDays
        }

        val averageCompletion = if (totalDays > 0) {
            (totalCompleted.toFloat() / totalDays * 100).roundToInt().toFloat()
        } else 0f

        _habitStats.value = HabitStats(
            averageCompletion = averageCompletion,
            longestStreak = maxStreak,
            missedDays = totalMissed,
            totalHabits = habits.size,
            completedDays = totalCompleted,
            totalDays = totalDays
        )
    }

    fun getHabitStats(habit: Habit): HabitStats? {
        return habitStatsCache[habit.id]
    }

    private fun calculateHabitStats(
        habit: Habit,
        startDate: Long,
        endDate: Long
    ): HabitStats {
        var completed = 0
        var missed = 0
        var currentStreak = 0
        var maxStreak = 0
        var totalDays = 0

        var currentDate = startDate
        while (currentDate <= endDate) {
            totalDays++
            when (habit.dailyStatus[currentDate]) {
                true -> {
                    completed++
                    currentStreak++
                    maxStreak = maxOf(maxStreak, currentStreak)
                }
                false -> {
                    missed++
                    currentStreak = 0
                }
                null -> {
                    if (currentDate < System.currentTimeMillis()) {
                        missed++
                        currentStreak = 0
                    }
                }
            }
            currentDate += 24 * 60 * 60 * 1000 // Добавляем один день
        }

        val stats = HabitStats(
            averageCompletion = if (totalDays > 0) {
                (completed.toFloat() / totalDays * 100).roundToInt().toFloat()
            } else 0f,
            longestStreak = maxStreak,
            missedDays = missed,
            completedDays = completed,
            totalDays = totalDays
        )

        habitStatsCache[habit.id] = stats
        return stats
    }
}
