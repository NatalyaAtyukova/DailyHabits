package com.habittracker.dailyhabits.model

data class HabitStats(
    val averageCompletion: Float = 0f,
    val longestStreak: Int = 0,
    val missedDays: Int = 0,
    val totalHabits: Int = 0,
    val completedDays: Int = 0,
    val totalDays: Int = 0
)
