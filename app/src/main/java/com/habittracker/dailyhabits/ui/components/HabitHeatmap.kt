package com.habittracker.dailyhabits.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.habittracker.dailyhabits.model.Habit
import java.util.*

@Composable
fun HabitHeatmap(habits: List<Habit>) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    calendar.set(year, Calendar.JANUARY, 1)

    val data = mutableMapOf<Long, Float>()
    for (habit in habits) {
        for ((date, value) in habit.dailyStatus) {
            data[date] = (data[date] ?: 0f) + value
        }
    }

    val maxActivity = data.values.maxOrNull() ?: 1f

    Column {
        // Заголовок года
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Дни недели
        Row(modifier = Modifier.fillMaxWidth()) {
            val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            weekDays.forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Сама тепловая карта
        LazyColumn {
            items(52) { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (day in 0..6) {
                        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                        if (calendar.get(Calendar.YEAR) == year) {
                            val activity = data[calendar.timeInMillis] ?: 0f
                            val color = getColorForActivity(activity, maxActivity)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(color)
                                    .padding(2.dp)
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
            }
        }
    }
}

private fun getColorForActivity(activity: Float, maxActivity: Float): Color {
    if (activity == 0f) return Color.LightGray.copy(alpha = 0.3f)
    val percentage = (activity / maxActivity).coerceIn(0f, 1f)
    return Color.Green.copy(alpha = percentage)
} 