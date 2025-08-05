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
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.ui.text.style.TextAlign
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import com.habittracker.dailyhabits.gui.components.HabitItem
import com.habittracker.dailyhabits.model.Habit
import androidx.navigation.NavController
import com.habittracker.dailyhabits.ui.components.AdBanner
import com.habittracker.dailyhabits.ui.components.InterstitialAdManager
import androidx.compose.ui.res.stringResource
import com.habittracker.dailyhabits.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    viewModel: HabitViewModel,
    onAddHabit: () -> Unit,
    onEditHabit: (habitId: Int) -> Unit,
    onOpenStats: () -> Unit,
    navController: NavController,
    interstitialAdManager: InterstitialAdManager
) {
    val habits by viewModel.filteredHabits.collectAsState()

    Scaffold(
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = {
                        interstitialAdManager.showAd((navController.context as? android.app.Activity) ?: return@FloatingActionButton) {
                            onOpenStats()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = stringResource(R.string.statistics_button),
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(
                    onClick = {
                        interstitialAdManager.showAd((navController.context as? android.app.Activity) ?: return@FloatingActionButton) {
                            onAddHabit()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_habit_button),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth())
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.your_habits),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = stringResource(R.string.empty_list_icon),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = stringResource(R.string.no_habits_yet),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.no_habits_description),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onAddHabit) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text(stringResource(R.string.add_habit))
                        }
                    }
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
                            onEditHabit = {
                                interstitialAdManager.showAd((navController.context as? android.app.Activity) ?: return@HabitItem) {
                                    onEditHabit(it)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
