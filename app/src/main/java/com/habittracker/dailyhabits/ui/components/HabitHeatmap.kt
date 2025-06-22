package com.habittracker.dailyhabits.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habittracker.dailyhabits.model.Habit
import com.habittracker.dailyhabits.model.HabitType
import java.util.*
import kotlin.math.roundToInt

private const val WEEKS_IN_HEATMAP = 52 // 1 год
private val CELL_SIZE = 16.dp
private val CELL_SPACING = 2.dp

@Composable
fun HabitHeatmap(
    habit: Habit,
    modifier: Modifier = Modifier
) {
    val (days, monthSpans) = remember(habit.dailyStatus) {
        generateHeatmapData(habit.dailyStatus)
    }
    val scrollState = rememberScrollState()

    Row(modifier = modifier) {
        // Статичные метки дней недели
        Column(
            modifier = Modifier.padding(top = 20.dp, end = 4.dp)
        ) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { dayLabel ->
                Box(
                    modifier = Modifier.height(CELL_SIZE + CELL_SPACING * 2),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = dayLabel, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Column {
            // Синхронно прокручиваемая строка месяцев
            Row(modifier = Modifier.horizontalScroll(scrollState)) {
                monthSpans.forEach { (month, span) ->
                    Box(
                        modifier = Modifier.width(CELL_SIZE * span),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = month, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Синхронно прокручиваемая сетка
            Row(modifier = Modifier.horizontalScroll(scrollState)) {
                (0 until WEEKS_IN_HEATMAP).forEach { weekIndex ->
                    Column {
                        (0..6).forEach { dayIndex ->
                            val day = days.getOrNull(weekIndex * 7 + dayIndex)
                            val color = day?.let {
                                getColorForStatus(it.status, habit.type, habit.targetValue)
                            } ?: Color.Transparent

                            Box(
                                modifier = Modifier
                                    .size(CELL_SIZE)
                                    .padding(CELL_SPACING)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun generateHeatmapData(dailyStatus: Map<Long, Float>): Pair<List<HeatmapDay>, List<Pair<String, Int>>> {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    val today = calendar.timeInMillis

    calendar.add(Calendar.DAY_OF_YEAR, -((WEEKS_IN_HEATMAP - 1) * 7))
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysToSubtract = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
    calendar.add(Calendar.DAY_OF_YEAR, -daysToSubtract)

    val heatmapDays = mutableListOf<HeatmapDay>()
    val monthLabels = mutableListOf<Pair<String, Int>>()
    val monthFormatter = java.text.SimpleDateFormat("LLLLL", Locale.getDefault())

    var currentMonth = -1
    for (i in 0 until WEEKS_IN_HEATMAP * 7) {
        val timestamp = calendar.timeInMillis
        val normalizedTimestamp = Habit.normalizeTimestamp(timestamp)
        val status = if (normalizedTimestamp > today) null else dailyStatus[normalizedTimestamp] ?: 0f
        heatmapDays.add(HeatmapDay(timestamp, status))

        val month = calendar.get(Calendar.MONTH)
        if (month != currentMonth) {
            currentMonth = month
            monthLabels.add(Pair(monthFormatter.format(calendar.time).uppercase(), 1))
        } else {
            if (monthLabels.isNotEmpty()) {
                val last = monthLabels.last()
                monthLabels[monthLabels.lastIndex] = last.copy(second = last.second + 1)
            }
        }
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    
    val weightedLabels = monthLabels.map {
        it.first to (it.second / 7.0).roundToInt().coerceAtLeast(1)
    }

    return Pair(heatmapDays, weightedLabels)
}

private data class HeatmapDay(val timestamp: Long, val status: Float?)

@Composable
internal fun getColorForStatus(status: Float?, type: HabitType, targetValue: Float?): Color {
    if (status == null) {
        return Color.Transparent // Будущие дни
    }
    if (status == 0f) {
        return MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // Пропущенные или невыполненные
    }

    return when (type) {
        HabitType.SIMPLE -> MaterialTheme.colorScheme.primary
        HabitType.MEASURABLE -> {
            val percentage = (status / (targetValue ?: 1f)).coerceIn(0f, 1f)
            lerp(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary, percentage)
        }
    }
}

// Линейная интерполяция между двумя цветами
private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    val red = (start.red + fraction * (stop.red - start.red))
    val green = (start.green + fraction * (stop.green - start.green))
    val blue = (start.blue + fraction * (stop.blue - start.blue))
    val alpha = (start.alpha + fraction * (stop.alpha - start.alpha))
    return Color(red, green, blue, alpha)
} 