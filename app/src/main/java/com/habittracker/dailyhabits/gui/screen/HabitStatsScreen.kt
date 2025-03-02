package com.habittracker.dailyhabits.gui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.habittracker.dailyhabits.model.Habit
import com.habittracker.dailyhabits.model.HabitStats
import com.habittracker.dailyhabits.viewmodel.HabitStatsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitStatsScreen(
    viewModel: HabitStatsViewModel,
    habits: List<Habit>,
    onNavigateBack: () -> Unit
) {
    var selectedPeriod by remember { mutableStateOf(StatsPeriod.WEEK) }

    LaunchedEffect(habits, selectedPeriod) {
        viewModel.calculateHabitStats(habits, selectedPeriod)
    }

    val habitStats by viewModel.habitStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Статистика привычек",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    PeriodSelector(selectedPeriod) { selectedPeriod = it }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Общая статистика
            item {
                OverallStatsCard(habitStats)
            }

            // График выполнения
            item {
                CompletionChartCard(habitStats)
            }

            // Статистика по отдельным привычкам
            item {
                Text(
                    text = "Статистика по привычкам",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            items(habits) { habit ->
                HabitStatsCard(habit, viewModel.getHabitStats(habit))
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: StatsPeriod,
    onPeriodSelected: (StatsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        StatsPeriod.values().forEach { period ->
            TextButton(
                onClick = { onPeriodSelected(period) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (selectedPeriod == period)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .background(
                        if (selectedPeriod == period)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            Color.Transparent
                    )
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = period.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun OverallStatsCard(stats: HabitStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatRow(
                icon = Icons.Default.Timeline,
                label = "Среднее выполнение",
                value = "${stats.averageCompletion.toInt()}%",
                color = MaterialTheme.colorScheme.primary
            )
            StatRow(
                icon = Icons.Default.Whatshot,
                label = "Лучшая серия",
                value = "${stats.longestStreak} дней",
                color = MaterialTheme.colorScheme.tertiary
            )
            StatRow(
                icon = Icons.Default.Warning,
                label = "Пропущено дней",
                value = "${stats.missedDays}",
                color = MaterialTheme.colorScheme.error
            )
            StatRow(
                icon = Icons.Default.CheckCircle,
                label = "Всего привычек",
                value = "${stats.totalHabits}",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun StatRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CompletionChartCard(stats: HabitStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Выполнение привычек",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            HabitPieChart(stats.averageCompletion, stats.missedDays)
        }
    }
}

@Composable
private fun HabitStatsCard(habit: Habit, stats: HabitStats?) {
    if (stats == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    icon = Icons.Default.Timeline,
                    value = "${stats.averageCompletion.toInt()}%",
                    label = "Выполнение",
                    color = MaterialTheme.colorScheme.primary
                )
                StatisticItem(
                    icon = Icons.Default.Whatshot,
                    value = "${stats.longestStreak}",
                    label = "Серия",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatisticItem(
                    icon = Icons.Default.Warning,
                    value = "${stats.missedDays}",
                    label = "Пропущено",
                    color = MaterialTheme.colorScheme.error
                )
            }
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

@Composable
private fun HabitPieChart(completedPercentage: Float, missedDays: Int) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(android.graphics.Color.TRANSPARENT)
                setTransparentCircleColor(android.graphics.Color.WHITE)
                setTransparentCircleAlpha(110)
                holeRadius = 58f
                transparentCircleRadius = 61f
                setDrawCenterText(true)
                rotationAngle = 0f
                isRotationEnabled = true
                isHighlightPerTapEnabled = true
                setEntryLabelColor(android.graphics.Color.WHITE)
                setEntryLabelTextSize(12f)
                setUsePercentValues(true)
                
                centerText = "Выполнение\n${completedPercentage.toInt()}%"
                setCenterTextSize(16f)
                setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)
                
                legend.apply {
                    isEnabled = true
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                    textSize = 12f
                }

                val entries = listOf(
                    PieEntry(completedPercentage, "Выполнено"),
                    PieEntry(100f - completedPercentage, "Пропущено")
                )

                val dataSet = PieDataSet(entries, "").apply {
                    colors = listOf(
                        android.graphics.Color.parseColor("#4CAF50"),  // Зеленый
                        android.graphics.Color.parseColor("#F44336")   // Красный
                    )
                    valueTextSize = 14f
                    valueTextColor = android.graphics.Color.WHITE
                    valueFormatter = PercentFormatter()
                }

                data = PieData(dataSet).apply {
                    setValueFormatter(PercentFormatter())
                }
                invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}

enum class StatsPeriod(val title: String, val days: Int) {
    WEEK("Неделя", 7),
    MONTH("Месяц", 30),
    YEAR("Год", 365)
}