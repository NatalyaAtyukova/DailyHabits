package com.habittracker.dailyhabits.gui.screen

import com.habittracker.dailyhabits.model.Habit
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview // Для Preview
import androidx.compose.material.icons.Icons // Для Icons
import androidx.compose.material.icons.filled.ArrowBack // Для Icons.Default.ArrowBack
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions // Для KeyboardOptions
import androidx.compose.material3.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(viewModel: HabitViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить привычку") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.addHabit(Habit(name = name, description = description))
                        onBack()
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Добавить привычку")
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewAddHabitScreen() {
    val fakeHabitDao = object : com.habittracker.dailyhabits.database.HabitDao {
        override fun getAllHabits() = kotlinx.coroutines.flow.flowOf(emptyList<Habit>())
        override suspend fun insertHabit(habit: Habit) {}
        override suspend fun deleteHabit(habit: Habit) {}
    }
    val fakeViewModel = com.habittracker.dailyhabits.viewmodel.HabitViewModel(fakeHabitDao)

    AddHabitScreen(viewModel = fakeViewModel, onBack = {})
}