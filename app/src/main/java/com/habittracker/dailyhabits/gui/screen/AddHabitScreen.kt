package com.habittracker.dailyhabits.gui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import com.habittracker.dailyhabits.model.Habit
import com.habittracker.dailyhabits.model.HabitType
import com.habittracker.dailyhabits.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHabitScreen(viewModel: HabitViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<Long?>(null) }
    var habitType by remember { mutableStateOf(HabitType.SIMPLE) }
    var targetValue by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var tagInput by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf<String?>(null) }
    var repeatDays by remember { mutableStateOf(emptySet<Int>()) }

    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val context = LocalContext.current

    val todayCalendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfDay = todayCalendar.timeInMillis

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новая привычка") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            item {
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
            }

            item {
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
            }

            item {
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
                                        deadline = calendar.timeInMillis
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
            }

            item {
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
            }

            item {
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
            }

            item {
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
            }

            item {
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
                                    Icons.Default.Clear,
                                    contentDescription = "Удалить тег",
                                    modifier = Modifier.clickable {
                                        tags = tags - tag
                                    }
                                )
                            }
                        )
                    }
                }
            }

            item {
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
            }

            item {
                Button(
                    onClick = {
                        viewModel.addHabit(
                            Habit(
                                name = name,
                                description = description,
                                timestamp = startOfDay,
                                deadline = deadline,
                                type = habitType,
                                targetValue = targetValue.toFloatOrNull(),
                                unit = unit.takeIf { it.isNotBlank() },
                                tags = tags,
                                reminderTime = reminderTime,
                                repeatDays = repeatDays.toList(),
                                dailyStatus = emptyMap()
                            )
                        )
                        onBack()
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Создать привычку", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}