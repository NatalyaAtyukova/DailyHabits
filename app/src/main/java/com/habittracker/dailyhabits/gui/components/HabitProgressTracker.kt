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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habittracker.dailyhabits.model.Habit
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@Composable
fun HabitProgressTracker(
    habit: Habit,
    onUpdateStatus: (date: Long, value: Float?) -> Unit
) {
    val deviceTime = System.currentTimeMillis()
    var currentPeriodStart by remember { mutableStateOf(
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = deviceTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    ) }

    android.util.Log.d("HabitProgressTracker", "Device time: ${Date(deviceTime)}")
    android.util.Log.d("HabitProgressTracker", "Current period start: ${Date(currentPeriodStart)}")

    // Определяем начальную и конечную дату для прогресса
    val startDate = habit.timestamp
    val endDate = habit.deadline ?: getStartOfDay(System.currentTimeMillis())

    // Получаем дни для отображения: только от даты создания до deadline/сегодня
    val days = mutableListOf<Long>()
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = getStartOfDay(startDate)
    while (calendar.timeInMillis <= endDate) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newStart = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = currentPeriodStart
                        add(Calendar.DAY_OF_YEAR, -7)
                    }.timeInMillis
                    if (newStart >= habit.timestamp) {
                        currentPeriodStart = newStart
                    }
                },
                enabled = currentPeriodStart > habit.timestamp
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Предыдущий период",
                    tint = if (currentPeriodStart > habit.timestamp)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
            
            Text(
                text = "Прогресс",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            
            IconButton(
                onClick = {
                    val newStart = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = currentPeriodStart
                        add(Calendar.DAY_OF_YEAR, 7)
                    }.timeInMillis
                    if (days.size == 7 && newStart <= endDate) {
                        currentPeriodStart = newStart
                    }
                },
                enabled = days.size == 7 && calendar.timeInMillis <= endDate
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Следующий период",
                    tint = if (days.size == 7 && calendar.timeInMillis <= endDate) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(days) { timestamp ->
                val normalizedTimestamp = getStartOfDay(timestamp)
                DayProgressItem(
                    timestamp = normalizedTimestamp,
                    habit = habit,
                    isToday = normalizedTimestamp == getStartOfDay(deviceTime),
                    onUpdateStatus = { date, value ->
                        onUpdateStatus(getStartOfDay(date), value)
                    }
                )
            }
        }
    }
}

private fun getStartOfDay(timestamp: Long): Long {
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
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
    onUpdateStatus: (date: Long, value: Float?) -> Unit
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
    val status: Boolean? = when {
        actualStatus != null -> actualStatus >= 1f
        isPastDay -> false
        else -> null
    }
    
    var showMenu by remember { mutableStateOf(false) }

    val backgroundColor = when (status) {
        true -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
        false -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
        null -> if (isPastDay) MaterialTheme.colorScheme.error.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        status == true -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
        status == false -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        isPastDay -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val contentColor = when (status) {
        true -> MaterialTheme.colorScheme.onTertiaryContainer
        false -> MaterialTheme.colorScheme.onErrorContainer
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val icon: ImageVector? = when (status) {
        true -> Icons.Default.Check
        false -> Icons.Default.Close
        null -> null
    }

    val clickableModifier = if (timestamp <= now) {
        Modifier.clickable { showMenu = true }
    } else {
        Modifier
    }

    Box(modifier = clickableModifier) {
        Column(
            modifier = Modifier
                .size(width = 48.dp, height = 64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dateFormatter.format(Date(timestamp)),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedVisibility(
                visible = status != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = "Status",
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (timestamp <= now) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Выполнено") },
                    onClick = {
                        onUpdateStatus(timestamp, 1f)
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Check, null) }
                )
                DropdownMenuItem(
                    text = { Text("Пропущено") },
                    onClick = {
                        onUpdateStatus(timestamp, 0f)
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Close, null) }
                )
                DropdownMenuItem(
                    text = { Text("Не учитывать") },
                    onClick = {
                        onUpdateStatus(timestamp, null)
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Remove, null) }
                )
            }
        }
    }
}