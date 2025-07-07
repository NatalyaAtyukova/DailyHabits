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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.habittracker.dailyhabits.model.Habit
import com.habittracker.dailyhabits.model.HabitStats
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.habittracker.dailyhabits.ui.components.AdBanner
import com.habittracker.dailyhabits.ui.components.InterstitialAdManager
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitStatsScreen(
    habitViewModel: HabitViewModel,
    onBack: () -> Unit,
    interstitialAdManager: InterstitialAdManager
) {
    val habits by habitViewModel.allHabits.collectAsState()
    var selectedPeriod by remember { mutableStateOf(StatsPeriod.WEEK) }

    LaunchedEffect(habits, selectedPeriod) {
        habitViewModel.calculateHabitStats(habits, selectedPeriod)
    }

    val habitStats by habitViewModel.habitStats.collectAsState()

    val context = LocalContext.current

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
                    IconButton(onClick = {
                        interstitialAdManager.showAd((context as? android.app.Activity) ?: return@IconButton) {
                            onBack()
                        }
                    }) {
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
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth())
        }
    ) { paddingValues ->
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QueryStats,
                        contentDescription = "Нет данных",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        "Нет данных для статистики",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Начните выполнять привычки, и здесь появится подробный анализ вашего прогресса.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    OverallStatsCard(habitStats)
                }
                item {
                    CompletionChartCard(habitStats)
                }
                item {
                    Text(
                        text = "Статистика по привычкам",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }

                items(habits) { habit ->
                    HabitStatsCard(habit, habitViewModel.getHabitStats(habit))
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

enum class StatsPeriod(val title: String, val days: Int) {
    WEEK("Неделя", 7),
    MONTH("Месяц", 30),
    ALL("Все время", -1) // -1 для обозначения всего периода
}

@Composable
private fun PeriodSelector(
    selectedPeriod: StatsPeriod,
    onPeriodSelected: (StatsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        StatsPeriod.values().forEach { period ->
            TextButton(
                onClick = { onPeriodSelected(period) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = if (selectedPeriod == period) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    contentColor = if (selectedPeriod == period) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatRow(Icons.Default.TrendingUp, "Среднее выполнение", "${stats.averageCompletion.toInt()}%", MaterialTheme.colorScheme.primary)
            StatRow(Icons.Default.Whatshot, "Лучшая серия", "${stats.longestStreak} дней", MaterialTheme.colorScheme.tertiary)
            StatRow(Icons.Default.HighlightOff, "Пропущено дней", "${stats.missedDays}", MaterialTheme.colorScheme.error)
            StatRow(Icons.Default.CheckCircle, "Всего выполнено", "${stats.completedDays} раз", MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun StatRow(icon: ImageVector, label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(imageVector = icon, contentDescription = label, tint = color)
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CompletionChartCard(stats: HabitStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Соотношение выполнения",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            HabitPieChart(stats)
        }
    }
}

@Composable
private fun HabitPieChart(stats: HabitStats) {
    val completed = stats.completedDays.toFloat()
    val missed = stats.missedDays.toFloat()

    val total = completed + missed
    if (total == 0f) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("Нет данных за выбранный период")
        }
        return
    }

    val entries = listOf(
        PieEntry(completed, "Выполнено"),
        PieEntry(missed, "Пропущено")
    )

    val colors = listOf(
        MaterialTheme.colorScheme.primary.toArgb(),
        MaterialTheme.colorScheme.errorContainer.toArgb()
    )
    
    val textColor = MaterialTheme.colorScheme.onPrimary.toArgb()

    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 58f
                setHoleColor(Color.Transparent.toArgb())
                transparentCircleRadius = 61f
                setUsePercentValues(true)
                animateY(1400)

                legend.apply {
                    verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                    horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                    orientation = Legend.LegendOrientation.VERTICAL
                    setDrawInside(false)
                    textSize = 12f
                }
            }
        },
        update = { chart ->
            val dataSet = PieDataSet(entries, "").apply {
                sliceSpace = 3f
                this.colors = colors
                valueTextSize = 12f
                valueTextColor = textColor
            }
            chart.data = PieData(dataSet).apply {
                setValueFormatter(PercentFormatter(chart))
            }
            chart.invalidate()
        },
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}

@Composable
private fun HabitStatsCard(habit: Habit, stats: HabitStats?) {
    if (stats == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem("Выполнено", "${stats.completedDays} дн.", MaterialTheme.colorScheme.primary)
                StatItem("Пропущено", "${stats.missedDays} дн.", MaterialTheme.colorScheme.error)
                StatItem("Серия", "${stats.longestStreak} дн.", MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())