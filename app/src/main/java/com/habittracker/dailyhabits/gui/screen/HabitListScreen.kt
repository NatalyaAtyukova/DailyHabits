package com.habittracker.dailyhabits.gui.screen

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    viewModel: HabitViewModel,
    onAddHabit: () -> Unit,
    onEditHabit: (habitId: Int) -> Unit,
    onOpenStats: () -> Unit
) {
    val habits by viewModel.filteredHabits.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()

    Scaffold(
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = onOpenStats,
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
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Ваши привычки",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Панель фильтров по тегам
            if (tags.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTag == null,
                            onClick = { viewModel.selectTag(null) },
                            label = { Text("Все") }
                        )
                    }
                    items(tags) { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { viewModel.selectTag(tag) },
                            label = { Text(tag) }
                        )
                    }
                }
            }

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
                    items(habits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onUpdateStatus = viewModel::updateHabitStatus,
                            onDeleteHabit = viewModel::deleteHabit,
                            onEditHabit = onEditHabit
                        )
                    }
                }
            }
        }
    }
}
