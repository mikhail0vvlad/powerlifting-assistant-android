package com.powerlifting.assistant.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class WorkoutStatus(val wire: String) {
    PLANNED("planned"),
    COMPLETED("completed"),
    MISSED("missed"),
    RESCHEDULED("rescheduled");

    companion object {
        fun parse(s: String?): WorkoutStatus =
            values().firstOrNull { it.wire.equals(s, ignoreCase = true) } ?: PLANNED
    }
}

sealed class ProgramSchedule {
    data class Weekdays(val days: Set<DayOfWeek>) : ProgramSchedule()
    data class Dates(val dates: List<LocalDate>) : ProgramSchedule()
}

data class TrainingProgram(
    val id: String,
    val name: String,
    val templateCode: String,
    val startDate: String,
    val weeks: Int,
    val isActive: Boolean,
    val schedule: ProgramSchedule? = null
)

data class ProgramExercise(
    val id: String,
    val exerciseName: String,
    val orderIndex: Int,
    val sets: Int,
    val reps: String,
    val percent1rm: Double? = null,
    val liftType: String
)

data class ProgramWorkout(
    val id: String,
    val date: String,
    val title: String,
    val status: String,
    val exercises: List<ProgramExercise>,
    val originalWorkoutId: String? = null
) {
    val statusEnum: WorkoutStatus get() = WorkoutStatus.parse(status)
}

data class ActiveProgram(
    val program: TrainingProgram? = null,
    val upcomingWorkouts: List<ProgramWorkout> = emptyList()
)

data class CalendarDay(
    val date: String,
    val title: String,
    val status: String,
    val workoutId: String? = null
) {
    val statusEnum: WorkoutStatus get() = WorkoutStatus.parse(status)
}

data class TrainingCalendar(
    val from: String,
    val to: String,
    val days: List<CalendarDay>
)
