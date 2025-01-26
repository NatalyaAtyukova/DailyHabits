package com.habittracker.dailyhabits.gui.screen

import android.app.DatePickerDialog
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import com.habittracker.dailyhabits.model.Habit
import androidx.compose.ui.tooling.preview.Preview
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(viewModel: HabitViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<Long?>(null) }
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val context = LocalContext.current // Теперь это внутри @Composable функции

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
                // Поле для названия
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

                // Поле для описания
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Поле для выбора даты
                TextField(
                    value = deadline?.let { dateFormatter.format(Date(it)) } ?: "",
                    onValueChange = { /* Поле только для чтения */ },
                    label = { Text("Срок выполнения") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Кнопка для выбора даты
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context, // Используем LocalContext.current внутри @Composable функции
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                deadline = calendar.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Выбрать дату")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка для добавления привычки
                Button(
                    onClick = {
                        viewModel.addHabit(
                            Habit(
                                name = name,
                                description = description,
                                timestamp = System.currentTimeMillis(),
                                deadline = deadline
                            )
                        )
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
        override suspend fun updateHabit(habit: Habit) {} // Добавлено для реализации интерфейса
    }
    val fakeViewModel = com.habittracker.dailyhabits.viewmodel.HabitViewModel(fakeHabitDao)

    MaterialTheme {
        AddHabitScreen(viewModel = fakeViewModel, onBack = {})
    }
}