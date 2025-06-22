package com.habittracker.dailyhabits.gui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.habittracker.dailyhabits.model.Habit
import com.habittracker.dailyhabits.model.HabitType
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditHabitScreen(
    habitViewModel: HabitViewModel,
    navController: NavController,
    habitId: Int
) {
    val habitState = habitViewModel.getHabitById(habitId).collectAsState(initial = null)
    val habit = habitState.value

    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    if (habit != null) {
        var name by remember { mutableStateOf(habit.name) }
        var description by remember { mutableStateOf(habit.description) }
        var deadline by remember { mutableStateOf(habit.deadline) }
        var habitType by remember { mutableStateOf(habit.type) }
        var targetValue by remember { mutableStateOf(habit.targetValue?.toString() ?: "") }
        var unit by remember { mutableStateOf(habit.unit ?: "") }
        var tags by remember { mutableStateOf(habit.tags) }
        var tagInput by remember { mutableStateOf("") }
        var reminderTime by remember { mutableStateOf(habit.reminderTime) }
        var repeatDays by remember { mutableStateOf(habit.repeatDays.toSet()) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Редактирование") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Срок выполнения",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (deadline != null) {
                            Text(
                                text = dateFormatter.format(Date(deadline!!)),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        FilledTonalButton(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        calendar.set(year, month, dayOfMonth)
                                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                                        calendar.set(Calendar.MINUTE, 0)
                                        calendar.set(Calendar.SECOND, 0)
                                        calendar.set(Calendar.MILLISECOND, 0)
                                        deadline = calendar.timeInMillis
                                        android.util.Log.d("EditHabitScreen", "Selected and normalized date: ${Date(deadline!!)}")
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (deadline == null) "Выбрать дату" else "Изменить дату")
                        }
                    }
                }

                // Переключатель типа привычки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Тип привычки:", style = MaterialTheme.typography.bodyLarge)
                    FilterChip(
                        selected = habitType == HabitType.SIMPLE,
                        onClick = { habitType = HabitType.SIMPLE },
                        label = { Text("Обычная") }
                    )
                    FilterChip(
                        selected = habitType == HabitType.MEASURABLE,
                        onClick = { habitType = HabitType.MEASURABLE },
                        label = { Text("Измеряемая") }
                    )
                }

                // Поля для измеряемой привычки
                if (habitType == HabitType.MEASURABLE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = targetValue,
                            onValueChange = { targetValue = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Цель") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Ед. изм.") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Поле для тегов
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = {
                        tagInput = it
                        if (it.contains(",")) {
                            val newTags = it.split(",")
                                .map { tag -> tag.trim() }
                                .filter { tag -> tag.isNotBlank() }
                            tags = (tags + newTags).distinct()
                            tagInput = ""
                        }
                    },
                    label = { Text("Теги (через запятую)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Отображение добавленных тегов
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = { },
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Удалить тег",
                                    modifier = Modifier.size(18.dp).clickable {
                                        tags = tags - tag
                                    }
                                )
                            }
                        )
                    }
                }

                // Настройка повторов и напоминаний
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Напоминания и повторы", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Выбор времени
                        val timePickerDialog = TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                reminderTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                            }, 12, 0, true
                        )

                        Button(onClick = { timePickerDialog.show() }) {
                            Text(reminderTime ?: "Выбрать время напоминания")
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Выбор дней недели
                        val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            weekDays.forEachIndexed { index, day ->
                                val dayOfWeek = index + 1
                                FilterChip(
                                    selected = repeatDays.contains(dayOfWeek),
                                    onClick = {
                                        repeatDays = if (repeatDays.contains(dayOfWeek)) {
                                            repeatDays - dayOfWeek
                                        } else {
                                            repeatDays + dayOfWeek
                                        }
                                    },
                                    label = { Text(day) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Отмена")
                    }

                    Button(
                        onClick = {
                            habitViewModel.updateHabit(
                                habit.copy(
                                    name = name,
                                    description = description,
                                    deadline = deadline,
                                    type = habitType,
                                    targetValue = targetValue.toFloatOrNull(),
                                    unit = unit.takeIf { it.isNotBlank() },
                                    tags = tags,
                                    reminderTime = reminderTime,
                                    repeatDays = repeatDays.toList()
                                )
                            )
                            navController.popBackStack()
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}