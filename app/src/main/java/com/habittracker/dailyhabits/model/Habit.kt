package com.habittracker.dailyhabits.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.habittracker.dailyhabits.database.Converters
import java.util.*

@Entity(tableName = "habits")
@TypeConverters(Converters::class) // Подключение конвертера для хранения сложных типов
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val timestamp: Long = normalizeTimestamp(System.currentTimeMillis()),
    val deadline: Long? = null, // Срок выполнения в виде timestamp

    // Новые поля для расширенного функционала
    val type: HabitType = HabitType.SIMPLE, // Тип привычки
    val targetValue: Float? = null,          // Целевое значение для измеряемых привычек
    val unit: String? = null,                // Единица измерения (напр., "км", "страниц")
    val tags: List<String> = emptyList(),    // Теги для категоризации
    val reminderTime: String? = null,        // Время напоминания (напр., "08:30")
    val repeatDays: List<Int> = emptyList(), // Дни недели для повтора (Пн=1, Вт=2...)

    // Статус выполнения теперь хранит Float.
    // Для SIMPLE: 1.0 = выполнено.
    // Для MEASURABLE: фактическое значение.
    val dailyStatus: Map<Long, Float> = emptyMap()
) {
    companion object {
        fun normalizeTimestamp(timestamp: Long): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return calendar.timeInMillis
        }
    }
}