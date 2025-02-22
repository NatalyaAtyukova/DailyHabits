package com.habittracker.dailyhabits.gui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onUpdateStatus: (Habit, Long, Boolean?) -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd.MM", Locale.getDefault())
    val today = getStartOfToday()

    val startDate = habit.timestamp // Показываем все дни от создания привычки
    val endDate = habit.deadline ?: today // Показываем до сегодняшнего дня, если нет дедлайна

    val totalDays = ((endDate - startDate) / (24 * 60 * 60 * 1000)).toInt() + 1
    val daysBetween = (0 until totalDays).map { startDate + it * 24 * 60 * 60 * 1000 }

    var habitStatus by remember { mutableStateOf(habit.dailyStatus.toMutableMap()) }
    var expandedDay by remember { mutableStateOf<Long?>(null) } // Храним дату, на которую нажали

    LaunchedEffect(habit.dailyStatus) {
        habitStatus = habit.dailyStatus.toMutableMap()
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(daysBetween) { date ->
            val normalizedDate = date / (24 * 60 * 60 * 1000) * (24 * 60 * 60 * 1000) // Округление до начала дня
            val status = habitStatus[normalizedDate]
            val isMissed = normalizedDate < today && status == null
            val color = when {
                status == true -> Color(0xFF4CAF50) // ✅ Зеленый - выполнено
                status == false -> Color(0xFFF44336) // ❌ Красный - пропущено
                isMissed -> Color(0xFFFFCDD2) // ⬜ Светло-красный - автоматически пропущено
                else -> Color(0xFFBDBDBD) // ⬜ Серый - не отмечено
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dateFormatter.format(Date(normalizedDate)),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(2.dp)
                        .border(1.dp, Color.Black, shape = MaterialTheme.shapes.small)
                        .background(color, shape = MaterialTheme.shapes.small)
                        .clickable { expandedDay = normalizedDate }, // Открываем меню
                    contentAlignment = Alignment.Center
                ) {
                    if (normalizedDate == today) {
                        Text(
                            text = "★",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                    }
                }

                DropdownMenu(
                    expanded = expandedDay == normalizedDate,
                    onDismissRequest = { expandedDay = null }
                ) {
                    DropdownMenuItem(
                        text = { Text("✅ Выполнено") },
                        onClick = {
                            habitStatus = habitStatus.toMutableMap().apply { put(normalizedDate, true) }
                            expandedDay = null
                            onUpdateStatus(habit.copy(dailyStatus = habitStatus), normalizedDate, true)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("❌ Пропущено") },
                        onClick = {
                            habitStatus = habitStatus.toMutableMap().apply { put(normalizedDate, false) }
                            expandedDay = null
                            onUpdateStatus(habit.copy(dailyStatus = habitStatus), normalizedDate, false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("⬜ Очистить") },
                        onClick = {
                            habitStatus = habitStatus.toMutableMap().apply { remove(normalizedDate) }
                            expandedDay = null
                            onUpdateStatus(habit.copy(dailyStatus = habitStatus), normalizedDate, null)
                        }
                    )
                }
            }
        }
    }
}

// Функция для получения начала сегодняшнего дня (00:00:00)
fun getStartOfToday(): Long {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}