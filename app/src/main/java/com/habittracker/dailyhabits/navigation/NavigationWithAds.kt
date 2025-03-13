package com.habittracker.dailyhabits.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habittracker.dailyhabits.ui.components.AdBanner
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import com.habittracker.dailyhabits.viewmodel.HabitStatsViewModel

@Composable
fun NavigationWithAds(
    viewModel: HabitViewModel,
    statsViewModel: HabitStatsViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AppNavigation(viewModel = viewModel, statsViewModel = statsViewModel)
            }
            
            // Рекламный баннер внизу экрана
            AdBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
} 