package com.powerlifting.assistant.data.mapper

import com.powerlifting.assistant.data.api.ActiveProgramResponse
import com.powerlifting.assistant.data.api.CalendarDayDto
import com.powerlifting.assistant.data.api.CalendarResponse
import com.powerlifting.assistant.data.api.ProgramExerciseDto
import com.powerlifting.assistant.data.api.ProgramWorkoutDto
import com.powerlifting.assistant.data.api.ScheduleDto
import com.powerlifting.assistant.data.api.TrainingProgramDto
import com.powerlifting.assistant.domain.model.ActiveProgram
import com.powerlifting.assistant.domain.model.CalendarDay
import com.powerlifting.assistant.domain.model.ProgramExercise
import com.powerlifting.assistant.domain.model.ProgramSchedule
import com.powerlifting.assistant.domain.model.ProgramWorkout
import com.powerlifting.assistant.domain.model.TrainingCalendar
import com.powerlifting.assistant.domain.model.TrainingProgram
import java.time.DayOfWeek
import java.time.LocalDate

fun ScheduleDto.toDomain(): ProgramSchedule? = when (type.lowercase()) {
    "weekdays" -> {
        val days = weekdays.orEmpty()
            .filter { it in 1..7 }
            .map { DayOfWeek.of(it) }
            .toSet()
        if (days.isEmpty()) null else ProgramSchedule.Weekdays(days)
    }
    "dates" -> {
        val parsed = dates.orEmpty().mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (parsed.isEmpty()) null else ProgramSchedule.Dates(parsed)
    }
    else -> null
}

fun ProgramSchedule.toDto(): ScheduleDto = when (this) {
    is ProgramSchedule.Weekdays -> ScheduleDto(
        type = "weekdays",
        weekdays = days.map { it.value }.sorted()
    )
    is ProgramSchedule.Dates -> ScheduleDto(
        type = "dates",
        dates = dates.map { it.toString() }
    )
}

fun TrainingProgramDto.toDomain() = TrainingProgram(
    id = id,
    name = name,
    templateCode = templateCode,
    startDate = startDate,
    weeks = weeks,
    isActive = isActive,
    schedule = schedule?.toDomain()
)

fun ProgramExerciseDto.toDomain() = ProgramExercise(
    id = id,
    exerciseName = exerciseName,
    orderIndex = orderIndex,
    sets = sets,
    reps = reps,
    percent1rm = percent1rm,
    liftType = liftType
)

fun ProgramWorkoutDto.toDomain() = ProgramWorkout(
    id = id,
    date = date,
    title = title,
    status = status,
    exercises = exercises.map { it.toDomain() },
    originalWorkoutId = originalWorkoutId
)

fun ActiveProgramResponse.toDomain() = ActiveProgram(
    program = program?.toDomain(),
    upcomingWorkouts = upcomingWorkouts.map { it.toDomain() }
)

fun CalendarDayDto.toDomain() = CalendarDay(
    date = date,
    title = title,
    status = status,
    workoutId = workoutId
)

fun CalendarResponse.toDomain() = TrainingCalendar(
    from = from,
    to = to,
    days = days.map { it.toDomain() }
)
