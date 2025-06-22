package com.habittracker.dailyhabits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.habittracker.dailyhabits.database.HabitDatabase
import com.habittracker.dailyhabits.navigation.Navigation
import com.habittracker.dailyhabits.ui.theme.DailyHabitsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = HabitDatabase.getDatabase(this)
        setContent {
            DailyHabitsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation(database)
                }
            }
        }
    }
}
