package com.habittracker.dailyhabits.gui.screen
import com.habittracker.dailyhabits.model.Habit
import android.graphics.Color
import android.graphics.Typeface
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.habittracker.dailyhabits.viewmodel.HabitStatsViewModel
import androidx.compose.material3.*

@Composable
fun HabitStatsScreen(viewModel: HabitStatsViewModel, habits: List<Habit>) {
    // ✅ Запускаем расчет статистики при загрузке экрана
    LaunchedEffect(habits) {
        viewModel.calculateHabitStats(habits)
    }

    val habitStats by viewModel.habitStats.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Habit Statistics",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Average Completion: ${habitStats.averageCompletion.toInt()}%", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Longest Streak: ${habitStats.longestStreak} days", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Missed Days: ${habitStats.missedDays}", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pie Chart
        HabitPieChart(habitStats.averageCompletion, habitStats.missedDays)
    }
}

@Composable
fun HabitPieChart(completedPercentage: Float, missedDays: Int) {
    AndroidView(factory = { context ->
        PieChart(context).apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
            setUsePercentValues(true)
            centerText = "Habit Completion"
            setCenterTextSize(16f)
            setCenterTextTypeface(Typeface.DEFAULT_BOLD)
            legend.isEnabled = true
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)

            val entries = listOf(
                PieEntry(completedPercentage, "Completed"),
                PieEntry(100 - completedPercentage, "Missed")
            )

            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(ColorTemplate.COLORFUL_COLORS[0], ColorTemplate.COLORFUL_COLORS[1])
                valueTextSize = 14f
            }

            data = PieData(dataSet)
            invalidate()
        }
    }, modifier = Modifier.fillMaxWidth().height(300.dp))
}