package com.habittracker.dailyhabits.model

enum class StatsPeriod(val title: String, val days: Int) {
    WEEK("Неделя", 7),
    MONTH("Месяц", 30),
    ALL("Все время", -1) // -1 для обозначения всего периода
} 