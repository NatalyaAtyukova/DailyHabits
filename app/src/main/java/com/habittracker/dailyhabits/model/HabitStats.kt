package com.habittracker.dailyhabits.model

data class HabitStats(
    val averageCompletion: Float,
    val longestStreak: Int,
    val missedDays: Int
)
