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
import com.habittracker.dailyhabits.ui.components.AdBanner
import com.habittracker.dailyhabits.ui.components.InterstitialAdManager
import androidx.compose.ui.res.stringResource
import com.habittracker.dailyhabits.R

// --- СТАНДАРТНЫЕ ПРИВЫЧКИ ---
data class StandardHabit(
    val name: String,
    val description: String = "",
    val type: HabitType = HabitType.SIMPLE,
    val targetValue: Float? = null,
    val unit: String? = null,
    val emoji: String
)

fun getStandardHabits(): List<StandardHabit> {
    return listOf(
        StandardHabit(
            name = "Пить воду",
            description = "Выпивать 6-8 стаканов воды в день",
            type = HabitType.SIMPLE,
            targetValue = null,
            unit = null,
            emoji = "💧"
        ),
        StandardHabit(
            name = "Зарядка",
            description = "Делать утреннюю разминку или упражнения",
            type = HabitType.SIMPLE,
            targetValue = null,
            unit = null,
            emoji = "🏃"
        ),
        StandardHabit(
            name = "Чтение",
            description = "Читать хотя бы 10 страниц в день",
            type = HabitType.SIMPLE,
            targetValue = null,
            unit = null,
            emoji = "📖"
        ),
        StandardHabit(
            name = "Медитация",
            description = "Медитировать 5-10 минут",
            type = HabitType.SIMPLE,
            targetValue = null,
            unit = null,
            emoji = "🧘"
        ),
        StandardHabit(
            name = "Прогулка",
            description = "Гулять на свежем воздухе не менее 30 минут",
            type = HabitType.SIMPLE,
            targetValue = null,
            unit = null,
            emoji = "🚶"
        ),
        StandardHabit(
            name = "Дневник",
            description = "Записывать мысли или вести дневник",
            type = HabitType.SIMPLE,
            targetValue = null,
            unit = null,
            emoji = "📝"
        ),
        StandardHabit(
            name = "Ранний подъем",
            description = "Вставать до 8:00",
            type = HabitType.SIMPLE,
            targetValue = null,
            unit = null,
            emoji = "🌞"
        ),
        StandardHabit(
            name = "Без сладкого",
            description = "Не есть сладкое в течение дня",
            type = HabitType.SIMPLE,
            targetValue = null,
            unit = null,
            emoji = "🍫"
        ),
        StandardHabit(
            name = "Фрукты/овощи",
            description = "Съесть 3 порции овощей или фруктов",
            type = HabitType.SIMPLE,
            targetValue = null,
            unit = null,
            emoji = "🥦"
        ),
        StandardHabit(
            name = "Спорт",
            description = "Заниматься спортом не менее 30 минут",
            type = HabitType.SIMPLE,
            targetValue = null,
            unit = null,
            emoji = "🏋"
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHabitScreen(viewModel: HabitViewModel, navController: NavController, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<Long?>(null) }
    var habitType by remember { mutableStateOf(HabitType.SIMPLE) }
    var targetValue by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var reminders by remember { mutableStateOf(listOf<String>()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var timePickerHour by remember { mutableStateOf(8) }
    var timePickerMinute by remember { mutableStateOf(0) }
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

    var nameError by remember { mutableStateOf(false) }
    var targetValueError by remember { mutableStateOf(false) }
    var unitError by remember { mutableStateOf(false) }

    // Вызываем getStandardHabits() внутри @Composable функции
    val standardHabits = getStandardHabits()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_habit)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth())
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
            // --- БЛОК СТАНДАРТНЫХ ПРИВЫЧЕК ---
            item {
                Text(stringResource(R.string.popular_habits), style = MaterialTheme.typography.titleMedium)
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
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text(stringResource(R.string.name_required)) },
                    isError = nameError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (nameError) Text(stringResource(R.string.name_required_error), color = MaterialTheme.colorScheme.error)
                        else Text(stringResource(R.string.name_example))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (nameError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (nameError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description)) },
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
                            text = stringResource(R.string.deadline),
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
                            Text(if (deadline == null) stringResource(R.string.select_date) else stringResource(R.string.change_date))
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
                    Text(stringResource(R.string.habit_type), style = MaterialTheme.typography.bodyLarge)
                    FilterChip(
                        selected = habitType == HabitType.SIMPLE,
                        onClick = { habitType = HabitType.SIMPLE },
                        label = { Text(stringResource(R.string.simple)) }
                    )
                    FilterChip(
                        selected = habitType == HabitType.MEASURABLE,
                        onClick = { habitType = HabitType.MEASURABLE },
                        label = { Text(stringResource(R.string.measurable)) }
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
                            onValueChange = {
                                targetValue = it.filter { c -> c.isDigit() || c == '.' }
                                targetValueError = false
                            },
                            label = { Text(stringResource(R.string.target_required)) },
                            isError = targetValueError,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            supportingText = {
                                if (targetValueError) Text(stringResource(R.string.target_error), color = MaterialTheme.colorScheme.error)
                                else Text(stringResource(R.string.target_example))
                            }
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {
                                unit = it
                                unitError = false
                            },
                            label = { Text(stringResource(R.string.unit_required)) },
                            isError = unitError,
                            modifier = Modifier.weight(1f),
                            supportingText = {
                                if (unitError) Text(stringResource(R.string.unit_error), color = MaterialTheme.colorScheme.error)
                                else Text(stringResource(R.string.unit_example))
                            }
                        )
                    }
                }
            }

            item {
                // Настройка повторов и напоминаний
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.reminders_and_repeats), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        // --- Список напоминаний ---
                        reminders.forEachIndexed { idx, time ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(time, style = MaterialTheme.typography.bodyLarge)
                                IconButton(onClick = { reminders = reminders.toMutableList().also { it.removeAt(idx) } }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.remove_reminder))
                                }
                            }
                        }
                        Button(onClick = { showTimePicker = true }) {
                            Text(stringResource(R.string.add_reminder))
                        }
                        if (showTimePicker) {
                            val context = LocalContext.current
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    val timeStr = String.format("%02d:%02d", hour, minute)
                                    if (timeStr !in reminders) reminders = reminders + timeStr
                                    showTimePicker = false
                                },
                                timePickerHour, timePickerMinute, true
                            ).show()
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // --- Новый выбор дней недели ---
                        val weekDays = listOf(
                            "Пн" to "🌑", "Вт" to "🌒", "Ср" to "🌓", "Чт" to "🌔", "Пт" to "🌕", "Сб" to "🌖", "Вс" to "🌞"
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            weekDays.forEachIndexed { index, (day, emoji) ->
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
                                    label = { Text("$emoji $day") }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        var valid = true
                        if (name.isBlank()) {
                            nameError = true
                            valid = false
                        }
                        if (habitType == HabitType.MEASURABLE) {
                            if (targetValue.isBlank() || targetValue.toFloatOrNull() == null) {
                                targetValueError = true
                                valid = false
                            }
                            if (unit.isBlank()) {
                                unitError = true
                                valid = false
                            }
                        }
                        if (!valid) return@Button
                        viewModel.addHabit(
                            Habit(
                                name = name,
                                description = description,
                                timestamp = startOfDay,
                                deadline = deadline,
                                type = habitType,
                                targetValue = targetValue.toFloatOrNull(),
                                unit = unit.takeIf { it.isNotBlank() },
                                tags = emptyList(),
                                reminderTime = reminders.firstOrNull(),
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