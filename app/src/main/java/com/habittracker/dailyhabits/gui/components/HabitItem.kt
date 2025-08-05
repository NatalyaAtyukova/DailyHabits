package com.habittracker.dailyhabits.gui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import com.habittracker.dailyhabits.model.Habit
import com.habittracker.dailyhabits.model.HabitType
import com.habittracker.dailyhabits.ui.components.HabitHeatmap
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitItem(
    habit: Habit,
    onUpdateStatus: (habit: Habit, date: Long, value: Float?) -> Unit,
    onDeleteHabit: (habit: Habit) -> Unit,
    onEditHabit: (habitId: Int) -> Unit
) {
    var showInputDialog by remember { mutableStateOf(false) }
    var inputValue by remember { mutableStateOf("") }
    val todayNormalized = remember { Habit.normalizeTimestamp(System.currentTimeMillis()) }
    var expanded by remember { mutableStateOf(false) }

    if (showInputDialog) {
        AlertDialog(
            onDismissRequest = { showInputDialog = false },
            title = { Text(stringResource(R.string.update_progress, habit.name)) },
            text = {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(stringResource(R.string.today_result, habit.unit)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val finalValue = inputValue.toFloatOrNull()
                        onUpdateStatus(habit, todayNormalized, finalValue)
                        showInputDialog = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showInputDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = habit.name, style = MaterialTheme.typography.titleLarge)
                Row {
                    IconButton(onClick = { onEditHabit(habit.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_habit_button))
                    }
                    IconButton(onClick = { onDeleteHabit(habit) }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_habit_button))
                    }
                }
            }
            if (habit.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (habit.type == HabitType.SIMPLE) {
                HabitProgressTracker(habit = habit) { date, value ->
                    onUpdateStatus(habit, Habit.normalizeTimestamp(date), value)
                }
            } else {
                val progress = habit.dailyStatus[todayNormalized] ?: 0f
                val progressPercentage = (progress / (habit.targetValue ?: 1f)).coerceIn(0f, 1f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.progress, progress.toString(), habit.targetValue.toString(), habit.unit))
                    Button(onClick = {
                        val todayStatus = habit.dailyStatus[todayNormalized]?.toString() ?: ""
                        inputValue = todayStatus
                        showInputDialog = true
                    }) {
                        Text(stringResource(R.string.update))
                    }
                }
                LinearProgressIndicator(
                    progress = progressPercentage,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HabitHeatmap(habit = habit)
                }
            }
        }
    }
}

@Composable
fun Chip(label: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MeasurableHabitTracker(habit: Habit, onUpdateClick: () -> Unit) {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val todayValue = habit.dailyStatus[today] ?: 0f
    val targetValue = habit.targetValue ?: 1f

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.today, "%.1f".format(todayValue), "%.1f".format(targetValue), habit.unit),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onUpdateClick) {
            Text(stringResource(R.string.update_result))
        }
    }
}

@Composable
private fun StatisticItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}