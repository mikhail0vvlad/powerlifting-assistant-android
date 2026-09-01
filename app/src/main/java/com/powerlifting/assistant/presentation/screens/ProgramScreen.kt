package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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

private val SectionSpacing = 16.dp
private val ControlSpacing = 8.dp

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(ControlSpacing)
        ) {
            Text("Программа", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Тренировочный план от ваших ПМ",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(SectionSpacing - ControlSpacing))

            if (state.profileMissingMaxes) {
                ElevatedCard(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(SectionSpacing),
                        verticalArrangement = Arrangement.spacedBy(ControlSpacing)
                    ) {
                        Text(
                            "Перед стартом нужно указать ПМ",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Заполните в профиле предельный максимум (1ПМ) в жиме, приседе и тяге.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            when {
                state.loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                active == null -> Text("Не удалось загрузить данные.")
                active.program == null -> {
                    ElevatedCard(
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(SectionSpacing),
                            verticalArrangement = Arrangement.spacedBy(ControlSpacing)
                        ) {
                            Text("У вас нет активной программы.")
                            Button(
                                onClick = { showCreateDialog = true },
                                enabled = !state.profileMissingMaxes,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Создать программу") }
                        }
                    }
                }
                else -> {
                    ElevatedCard(
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(SectionSpacing),
                            verticalArrangement = Arrangement.spacedBy(ControlSpacing / 2)
                        ) {
                            Text(active.program.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Старт: ${active.program.startDate} • ${active.program.weeks} нед.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (active.upcomingWorkouts.isEmpty()) {
                        ElevatedCard(
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(SectionSpacing),
                                verticalArrangement = Arrangement.spacedBy(ControlSpacing)
                            ) {
                                Text(
                                    "Программа завершена",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Все тренировки пройдены или пропущены. Создайте новую программу.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Button(
                                    onClick = { showCreateDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Новая программа")
                                }
                            }
                        }
                    } else {
                        Text(
                            "Тренировки",
                            style = MaterialTheme.typography.titleMedium
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(active.upcomingWorkouts, key = { it.id }) { w ->
                                WorkoutCard(w, onClick = { actionWorkout = w })
                            }
                        }

                        OutlinedButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Новая программа") }
                    }
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            if (state.mutating) {
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
        DatePickerModal(
            initialDate = parseLocalDate(w.date) ?: LocalDate.now(),
            confirmText = "Перенести",
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
        Column(
            modifier = Modifier.padding(SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(ControlSpacing / 2)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(w.title, style = titleStyle, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(ControlSpacing))
                AssistChip(
                    onClick = {},
                    label = { Text(statusLabel, color = statusColor) },
                    enabled = false
                )
            }
            Text(
                w.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(ControlSpacing / 2))
            w.exercises.sortedBy { it.orderIndex }.forEach { ex ->
                val percent = ex.percent1rm?.let { " • ${String.format("%.0f", it * 100)}% 1ПМ" } ?: ""
                Text(
                    "• ${ex.exerciseName}: ${ex.sets}x${ex.reps}$percent",
                    style = MaterialTheme.typography.bodySmall
                )
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
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(ControlSpacing)
            ) {
                Text(workout.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    workout.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(ControlSpacing))

                Text(
                    "Что сделать с этой тренировкой?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(ControlSpacing))

                FilledTonalButton(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("✅ Отметить выполненной") }

                OutlinedButton(
                    onClick = onReschedule,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("⏭ Перенести на другую дату") }

                if (workout.statusEnum == WorkoutStatus.PLANNED) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("❌ Пропустить") }
                }

                Spacer(Modifier.height(ControlSpacing))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Отмена") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateProgramDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, Int, Set<DayOfWeek>) -> Unit
) {
    var weeks by remember { mutableStateOf(4) }
    var selectedDays by remember {
        mutableStateOf(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
    }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать программу", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(SectionSpacing)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(ControlSpacing)) {
                    Text(
                        "Тренировочные дни недели",
                        style = MaterialTheme.typography.titleSmall
                    )
                    WeekdayGrid(
                        selected = selectedDays,
                        onToggle = { day ->
                            selectedDays = if (day in selectedDays) selectedDays - day else selectedDays + day
                        }
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(ControlSpacing)) {
                    Text(
                        "Длительность: $weeks нед.",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Slider(
                        value = weeks.toFloat(),
                        onValueChange = { weeks = it.toInt() },
                        valueRange = 1f..12f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(ControlSpacing)) {
                    Text(
                        "Дата старта",
                        style = MaterialTheme.typography.titleSmall
                    )
                    DateField(
                        date = selectedDate,
                        onClick = { showDatePicker = true }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedDays.isNotEmpty(),
                onClick = { onConfirm(selectedDate, weeks, selectedDays) }
            ) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )

    if (showDatePicker) {
        DatePickerModal(
            initialDate = selectedDate,
            confirmText = "Выбрать",
            onDismiss = { showDatePicker = false },
            onPick = {
                selectedDate = it
                showDatePicker = false
            }
        )
    }
}

@Composable
private fun WeekdayGrid(selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    val order = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )
    val columns = 4
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ControlSpacing)
    ) {
        order.chunked(columns).forEach { rowDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ControlSpacing)
            ) {
                rowDays.forEach { day ->
                    WeekdayChip(
                        day = day,
                        selected = day in selected,
                        onToggle = onToggle,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - rowDays.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WeekdayChip(
    day: DayOfWeek,
    selected: Boolean,
    onToggle: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier
) {
    val label = day.getDisplayName(TextStyle.SHORT, Locale("ru")).take(2)
    FilterChip(
        selected = selected,
        onClick = { onToggle(day) },
        label = {
            Text(
                label,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(date: LocalDate, onClick: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")) }
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SectionSpacing, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ControlSpacing)
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                date.format(formatter),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    initialDate: LocalDate,
    confirmText: String,
    onDismiss: () -> Unit,
    onPick: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@TextButton
                    val date = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                    onPick(date)
                }
            ) { Text(confirmText) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun parseLocalDate(s: String): LocalDate? = runCatching { LocalDate.parse(s) }.getOrNull()
