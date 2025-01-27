package com.habittracker.dailyhabits.gui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.dailyhabits.model.Habit
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitProgressTracker(
    habit: Habit,
    onUpdateStatus: (Habit, Long, Boolean) -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd.MM", Locale.getDefault())
    val startDate = habit.timestamp
    val endDate = habit.deadline ?: System.currentTimeMillis()
    val totalDays = ((endDate - startDate) / (24 * 60 * 60 * 1000) + 1).toInt()
    val currentDate = System.currentTimeMillis()

    // Список всех дат от начала до конца
    val daysBetween = (0 until totalDays).map { startDate + it * 24 * 60 * 60 * 1000 }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(daysBetween) { date ->
            val isCompleted = habit.dailyStatus[date] ?: false
            val isToday = dateFormatter.format(Date(date)) == dateFormatter.format(Date(currentDate))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dateFormatter.format(Date(date)),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                        .background(
                            color = if (isCompleted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        )
                        .clickable {
                            if (date <= currentDate) { // Можно отмечать только прошедшие или текущие дни
                                onUpdateStatus(habit, date, !isCompleted)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isToday) {
                        Text(
                            text = "★",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}