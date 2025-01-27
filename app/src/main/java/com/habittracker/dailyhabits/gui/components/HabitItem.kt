package com.habittracker.dailyhabits.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habittracker.dailyhabits.model.Habit
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitItem(
    habit: Habit,
    onDelete: (Habit) -> Unit,
    onEdit: (Habit) -> Unit,
    onUpdateStatus: (Habit, Long, Boolean) -> Unit // Добавлено
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    // Преобразуем текущее время к полуночи текущего дня
    val currentDate = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Проверяем статус привычки за текущий день
    val isCompletedToday = habit.dailyStatus[currentDate] ?: false

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name.ifBlank { "Без названия" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (habit.description.isNotBlank()) {
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    IconButton(onClick = { onEdit(habit) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать привычку")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить привычку")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Создано: ${dateFormatter.format(Date(habit.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                habit.deadline?.let {
                    Text(
                        text = "Срок: ${dateFormatter.format(Date(it))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Checkbox(
                    checked = isCompletedToday,
                    onCheckedChange = { isChecked ->
                        onUpdateStatus(habit, currentDate, isChecked) // Передаём округлённую дату
                    }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить привычку?") },
            text = { Text("Вы уверены, что хотите удалить привычку \"${habit.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(habit)
                    showDeleteDialog = false
                }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}