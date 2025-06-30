package com.habittracker.dailyhabits.gui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ListAlt
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController

// --- СТАНДАРТНЫЕ ПРИВЫЧКИ ---
data class StandardHabit(
    val name: String,
    val description: String = "",
    val type: HabitType = HabitType.SIMPLE,
    val targetValue: Float? = null,
    val unit: String? = null,
    val emoji: String
)

val standardHabits = listOf(
    StandardHabit("Пить воду", "Выпивать 6-8 стаканов воды в день", HabitType.SIMPLE, null, null, "💧"),
    StandardHabit("Зарядка", "Делать утреннюю разминку или упражнения", HabitType.SIMPLE, null, null, "🏃"),
    StandardHabit("Чтение", "Читать хотя бы 10 страниц в день", HabitType.SIMPLE, null, null, "📖"),
    StandardHabit("Медитация", "Медитировать 5-10 минут", HabitType.SIMPLE, null, null, "🧘"),
    StandardHabit("Прогулка", "Гулять на свежем воздухе не менее 30 минут", HabitType.SIMPLE, null, null, "🚶"),
    StandardHabit("Дневник", "Записывать мысли или вести дневник", HabitType.SIMPLE, null, null, "📝"),
    StandardHabit("Ранний подъем", "Вставать до 8:00", HabitType.SIMPLE, null, null, "🌞"),
    StandardHabit("Без сладкого", "Не есть сладкое в течение дня", HabitType.SIMPLE, null, null, "🍫"),
    StandardHabit("Фрукты/овощи", "Съесть 3 порции овощей или фруктов", HabitType.SIMPLE, null, null, "🥦"),
    StandardHabit("Спорт", "Заниматься спортом не менее 30 минут", HabitType.SIMPLE, null, null, "🏋")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHabitScreen(viewModel: HabitViewModel, navController: NavController, onBack: () -> Unit) {
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
    var showSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Для выделения выбранной стандартной привычки ---
    var selectedStandard by remember { mutableStateOf<String?>(null) }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            // --- БЛОК СТАНДАРТНЫХ ПРИВЫЧЕК ---
            item {
                Text("Популярные привычки", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    standardHabits.forEach { habit ->
                        val isSelected = selectedStandard == habit.name
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                name = habit.name
                                description = habit.description
                                habitType = habit.type
                                targetValue = habit.targetValue?.toString() ?: ""
                                unit = habit.unit ?: ""
                                selectedStandard = habit.name
                            },
                            label = { Text("${habit.emoji} ${habit.name}") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

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
                        navController.previousBackStackEntry?.savedStateHandle?.set("habit_created", true)
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
        // --- SNACKBAR ---
        if (showSnackbar) {
            LaunchedEffect(showSnackbar) {
                snackbarHostState.showSnackbar(
                    message = "Привычка добавлена!",
                    withDismissAction = true
                )
                kotlinx.coroutines.delay(1200)
                showSnackbar = false
                onBack()
            }
        }
    }
}