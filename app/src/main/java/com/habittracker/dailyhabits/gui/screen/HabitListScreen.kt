package com.habittracker.dailyhabits.gui.screen

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import com.habittracker.dailyhabits.gui.components.HabitItem
import com.habittracker.dailyhabits.model.Habit

@Composable
fun HabitListScreen(
    viewModel: HabitViewModel,
    onAddHabit: () -> Unit,
    onEditHabit: (Habit) -> Unit,
    onOpenStats: () -> Unit // ✅ Добавлен параметр для открытия статистики
) {
    val habits by viewModel.allHabits.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = onOpenStats, // ✅ Кнопка статистики
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Статистика",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(
                    onClick = onAddHabit,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить привычку",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Ваши привычки",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Список привычек пуст. Добавьте новую привычку!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(habits) { habit ->
                        // Рассчитываем прогресс привычки перед передачей в HabitItem
                        val (progress, skippedDays, streak) = viewModel.calculateProgress(habit)

                        HabitItem(
                            habit = habit,
                            onDelete = { viewModel.deleteHabit(it) },
                            onEdit = {
                                viewModel.editHabit(habit) // ✅ Теперь сохраняем редактируемую привычку
                                onEditHabit(habit) // Навигация к экрану редактирования
                            },
                            onUpdateStatus = { habit, date, status -> viewModel.updateHabitStatus(habit, date, status) },
                            progress = progress,
                            skippedDays = skippedDays,
                            streak = streak // ✅ Передаем streak в HabitItem
                        )
                    }
                }
            }
        }
    }
}
