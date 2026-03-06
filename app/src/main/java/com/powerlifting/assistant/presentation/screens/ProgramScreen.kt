package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerlifting.assistant.domain.model.ProgramWorkout
import com.powerlifting.assistant.domain.model.WorkoutStatus
import com.powerlifting.assistant.presentation.viewmodel.ProgramViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramScreen(vm: ProgramViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var actionWorkout by remember { mutableStateOf<ProgramWorkout?>(null) }
    var rescheduleWorkout by remember { mutableStateOf<ProgramWorkout?>(null) }

    val active = state.active

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Программа", style = MaterialTheme.typography.headlineSmall)
            Text("Тренировочный план от ваших ПМ", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Spacer(Modifier.height(16.dp))

            if (state.profileMissingMaxes) {
                ElevatedCard(shape = MaterialTheme.shapes.large) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Перед стартом нужно указать ПМ", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Заполните в профиле предельный максимум (1ПМ) в жиме, приседе и тяге.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            when {
                state.loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                active == null -> Text("Не удалось загрузить данные.")
                active.program == null -> {
                    ElevatedCard(shape = MaterialTheme.shapes.large) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("У вас нет активной программы.")
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { showCreateDialog = true },
                                enabled = !state.profileMissingMaxes,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Создать программу") }
                        }
                    }
                }
                else -> {
                    ElevatedCard(shape = MaterialTheme.shapes.large) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(active.program.name, style = MaterialTheme.typography.titleMedium)
                            Text("Старт: ${active.program.startDate} • ${active.program.weeks} нед.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    if (active.upcomingWorkouts.isEmpty()) {
                        ElevatedCard(shape = MaterialTheme.shapes.large) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Программа завершена", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(6.dp))
                                Text("Все тренировки пройдены или пропущены. Создайте новую программу.", style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(10.dp))
                                Button(onClick = { showCreateDialog = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Новая программа")
                                }
                            }
                        }
                    } else {
                        Text("Тренировки", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            items(active.upcomingWorkouts, key = { it.id }) { w ->
                                WorkoutCard(w, onClick = { actionWorkout = w })
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Новая программа") }
                    }
                }
            }

            state.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            if (state.mutating) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }

    if (showCreateDialog) {
        CreateProgramDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { startDate, weeks, weekdays ->
                vm.generate(startDate = startDate, weeks = weeks, weekdays = weekdays)
                showCreateDialog = false
            }
        )
    }

    actionWorkout?.let { w ->
        WorkoutActionDialog(
            workout = w,
            onDismiss = { actionWorkout = null },
            onComplete = {
                vm.complete(w.id)
                actionWorkout = null
            },
            onReschedule = {
                rescheduleWorkout = w
                actionWorkout = null
            },
            onSkip = {
                vm.skip(w.id)
                actionWorkout = null
            }
        )
    }

    rescheduleWorkout?.let { w ->
        DatePickerDialogSheet(
            initialDate = parseLocalDate(w.date) ?: LocalDate.now(),
            onDismiss = { rescheduleWorkout = null },
            onPick = { picked ->
                vm.reschedule(w.id, picked)
                rescheduleWorkout = null
            }
        )
    }
}

@Composable
private fun WorkoutCard(w: ProgramWorkout, onClick: () -> Unit) {
    val statusEnum = w.statusEnum
    val (statusLabel, statusColor) = when (statusEnum) {
        WorkoutStatus.PLANNED -> "Запланирована" to MaterialTheme.colorScheme.primary
        WorkoutStatus.COMPLETED -> "✅ Выполнена" to MaterialTheme.colorScheme.tertiary
        WorkoutStatus.MISSED -> "❌ Пропущена" to MaterialTheme.colorScheme.error
        WorkoutStatus.RESCHEDULED -> "↪ Перенесена" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    val titleStyle = if (statusEnum == WorkoutStatus.COMPLETED || statusEnum == WorkoutStatus.RESCHEDULED) {
        MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.LineThrough)
    } else {
        MaterialTheme.typography.titleMedium
    }

    ElevatedCard(
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
        enabled = statusEnum == WorkoutStatus.PLANNED || statusEnum == WorkoutStatus.MISSED
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(w.title, style = titleStyle, modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text(statusLabel, color = statusColor) }, enabled = false)
            }
            Text(w.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(10.dp))
            w.exercises.sortedBy { it.orderIndex }.forEach { ex ->
                val percent = ex.percent1rm?.let { " • ${String.format("%.0f", it * 100)}% 1ПМ" } ?: ""
                Text("• ${ex.exerciseName}: ${ex.sets}x${ex.reps}$percent", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun WorkoutActionDialog(
    workout: ProgramWorkout,
    onDismiss: () -> Unit,
    onComplete: () -> Unit,
    onReschedule: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(workout.title) },
        text = {
            Column {
                Text(workout.date, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                Text("Что сделать с этой тренировкой?")
            }
        },
        confirmButton = {
            Column {
                TextButton(onClick = onComplete, modifier = Modifier.fillMaxWidth()) { Text("✅ Отметить выполненной") }
                TextButton(onClick = onReschedule, modifier = Modifier.fillMaxWidth()) { Text("⏭ Перенести на другую дату") }
                if (workout.statusEnum == WorkoutStatus.PLANNED) {
                    TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) { Text("❌ Пропустить") }
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateProgramDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, Int, Set<DayOfWeek>) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    var weeks by remember { mutableStateOf(4) }
    var selectedDays by remember {
        mutableStateOf(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать программу") },
        text = {
            Column {
                Text("Тренировочные дни недели:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                FlowWeekdayPicker(selected = selectedDays, onToggle = { day ->
                    selectedDays = if (day in selectedDays) selectedDays - day else selectedDays + day
                })

                Spacer(Modifier.height(16.dp))
                Text("Длительность: $weeks нед.", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = weeks.toFloat(),
                    onValueChange = { weeks = it.toInt() },
                    valueRange = 1f..12f,
                    steps = 10
                )

                Spacer(Modifier.height(8.dp))
                Text("Дата старта:", style = MaterialTheme.typography.bodyMedium)
                DatePicker(state = datePickerState, showModeToggle = false, title = null, headline = null)
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedDays.isNotEmpty() && datePickerState.selectedDateMillis != null,
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@TextButton
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    onConfirm(date, weeks, selectedDays)
                }
            ) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun FlowWeekdayPicker(selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    val order = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        order.forEach { day ->
            val label = day.getDisplayName(TextStyle.SHORT, Locale("ru")).take(2)
            FilterChip(
                selected = day in selected,
                onClick = { onToggle(day) },
                label = { Text(label) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogSheet(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onPick: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@TextButton
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    onPick(date)
                }
            ) { Text("Перенести") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun parseLocalDate(s: String): LocalDate? = runCatching { LocalDate.parse(s) }.getOrNull()
