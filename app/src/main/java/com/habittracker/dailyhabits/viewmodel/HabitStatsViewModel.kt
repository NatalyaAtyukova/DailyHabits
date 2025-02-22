package com.habittracker.dailyhabits.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.habittracker.dailyhabits.model.Habit
import com.habittracker.dailyhabits.model.HabitStats
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import kotlin.math.max

class HabitStatsViewModel : ViewModel() {

    private val _habitStats = MutableStateFlow(HabitStats(0f, 0, 0))
    val habitStats: StateFlow<HabitStats> = _habitStats.asStateFlow()

    fun calculateHabitStats(habits: List<Habit>) {
        val totalDays = habits.sumOf { habit ->
            habit.dailyStatus.size
        }.coerceAtLeast(1)

        val completedDays = habits.sumOf { habit ->
            habit.dailyStatus.count { it.value }
        }

        val missedDays = totalDays - completedDays
        val averageCompletion = (completedDays.toFloat() / totalDays.toFloat()) * 100

        val longestStreak = habits.maxOfOrNull { habit ->
            habit.dailyStatus.entries.fold(0 to 0) { (maxStreak, currentStreak), entry ->
                if (entry.value) {
                    val newStreak = currentStreak + 1
                    maxOf(maxStreak, newStreak) to newStreak
                } else {
                    maxStreak to 0
                }
            }.first
        } ?: 0

        _habitStats.update {
            HabitStats(
                averageCompletion = averageCompletion,
                longestStreak = longestStreak,
                missedDays = missedDays
            )
        }
    }
}
