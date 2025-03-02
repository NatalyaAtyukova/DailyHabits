package com.habittracker.dailyhabits.gui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habittracker.dailyhabits.model.Habit
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@Composable
fun HabitProgressTracker(
    habit: Habit,
    onUpdateStatus: (Habit, Long, Boolean?) -> Unit
) {
    val deviceTime = System.currentTimeMillis()
    val today = Calendar.getInstance().apply {
        timeInMillis = deviceTime
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    android.util.Log.d("HabitProgressTracker", "Device time: ${Date(deviceTime)}")
    android.util.Log.d("HabitProgressTracker", "Today start: ${Date(today.timeInMillis)}")

    // Определяем конечную дату (deadline или 7 дней если deadline не установлен)
    val endDate = habit.deadline ?: Calendar.getInstance().apply {
        timeInMillis = today.timeInMillis
        add(Calendar.DAY_OF_YEAR, 6) // 7 дней включая сегодня
    }.timeInMillis

    // Получаем дни от сегодня до deadline (но не больше 7 дней)
    val days = mutableListOf<Long>()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = today.timeInMillis

    while (calendar.timeInMillis <= endDate && days.size < 7) {
        days.add(calendar.timeInMillis)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    android.util.Log.d("HabitProgressTracker", "Days to display: ${days.map { Date(it) }}")
    android.util.Log.d("HabitProgressTracker", "Habit ${habit.id} status: ${habit.dailyStatus.map { (date, status) -> "${Date(date)}: $status" }}")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp)
    ) {
        Text(
            text = "Прогресс",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(days) { timestamp ->
                val normalizedTimestamp = getStartOfDay(timestamp)
                DayProgressItem(
                    timestamp = normalizedTimestamp,
                    habit = habit,
                    isToday = normalizedTimestamp == getStartOfDay(today.timeInMillis),
                    onUpdateStatus = onUpdateStatus
                )
            }
        }
    }
}

private fun getStartOfDay(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@Composable
private fun DayProgressItem(
    timestamp: Long,
    habit: Habit,
    isToday: Boolean,
    onUpdateStatus: (Habit, Long, Boolean?) -> Unit
) {
    val deviceTime = System.currentTimeMillis()
    val dateFormatter = SimpleDateFormat("EE\ndd", Locale.getDefault())
    val actualStatus = habit.dailyStatus[timestamp]
    val now = getStartOfDay(deviceTime)
    val isPastDay = timestamp < (now - 24 * 60 * 60 * 1000) // Вчера и раньше
    
    android.util.Log.d("DayProgressItem", """
        |Day: ${Date(timestamp)}
        |Device time: ${Date(deviceTime)}
        |Now: ${Date(now)}
        |Is past day: $isPastDay
        |Actual status: $actualStatus
    """.trimMargin())
    
    // Если день прошел и статус не установлен, считаем его пропущенным
    val status = when {
        actualStatus != null -> actualStatus
        isPastDay -> false
        else -> null
    }
    
    var showMenu by remember { mutableStateOf(false) }

    val backgroundColor = when (status) {
        true -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
        false -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
        null -> if (isPastDay) MaterialTheme.colorScheme.error.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        status == true -> MaterialTheme.colorScheme.tertiary
        status == false -> MaterialTheme.colorScheme.error
        isPastDay -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val icon = when (status) {
        true -> Icons.Default.Check
        false -> Icons.Default.Close
        null -> if (isPastDay) Icons.Default.Warning else null
    }

    val iconTint = when (status) {
        true -> MaterialTheme.colorScheme.tertiary
        false -> MaterialTheme.colorScheme.error
        null -> if (isPastDay) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopStart)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = if (isToday) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { showMenu = true }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = dateFormatter.format(Date(timestamp)).uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (icon != null) backgroundColor else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = when (status) {
                            true -> "Выполнено"
                            false -> "Пропущено"
                            null -> if (isPastDay) "Автоматически пропущено" else "Не отмечено"
                        },
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Выполнено") },
                onClick = {
                    android.util.Log.d("DayProgressItem", "Setting status to TRUE for ${Date(timestamp)}")
                    onUpdateStatus(habit, timestamp, true)
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
            DropdownMenuItem(
                text = { Text("Пропущено") },
                onClick = {
                    android.util.Log.d("DayProgressItem", "Setting status to FALSE for ${Date(timestamp)}")
                    onUpdateStatus(habit, timestamp, false)
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
            DropdownMenuItem(
                text = { Text("Не отмечено") },
                onClick = {
                    android.util.Log.d("DayProgressItem", "Setting status to NULL for ${Date(timestamp)}")
                    onUpdateStatus(habit, timestamp, null)
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }
    }
}