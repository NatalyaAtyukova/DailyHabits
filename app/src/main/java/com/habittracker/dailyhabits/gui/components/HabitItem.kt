package com.habittracker.dailyhabits.gui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habittracker.dailyhabits.model.Habit

@Composable
fun HabitItem(habit: Habit, onDelete: (Habit) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { /* TODO: Отметить выполнение привычки */ },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = habit.name,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge
        )
        IconButton(onClick = { onDelete(habit) }) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить привычку")
        }
    }
}