package com.habittracker.dailyhabits.gui.screen

import android.app.DatePickerDialog
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import com.habittracker.dailyhabits.model.Habit
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    viewModel: HabitViewModel,
    habitId: Int,
    onBack: () -> Unit
) {
    val habit = produceState<Habit?>(initialValue = null, habitId) {
        value = viewModel.getHabitById(habitId)
    }.value

    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    habit?.let { currentHabit ->
        var name by remember { mutableStateOf(currentHabit.name) }
        var description by remember { mutableStateOf(currentHabit.description) }
        var deadline by remember { mutableStateOf(currentHabit.deadline) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Редактировать привычку") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    }
                )
            }
        ) { innerPadding ->
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
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // Поле для описания
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
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
                            context,
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

                // Кнопка для сохранения изменений
                Button(
                    onClick = {
                        viewModel.updateHabit(
                            currentHabit.copy(
                                name = name,
                                description = description,
                                deadline = deadline
                            )
                        )
                        onBack()
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить изменения")
                }
            }
        }
    } ?: run {
        // Если привычка не найдена
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Привычка не найдена", style = MaterialTheme.typography.bodyLarge)
        }
    }
}