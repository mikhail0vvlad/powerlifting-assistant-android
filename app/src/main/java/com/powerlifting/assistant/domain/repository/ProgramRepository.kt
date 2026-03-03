package com.powerlifting.assistant.domain.repository

import com.powerlifting.assistant.domain.model.ActiveProgram
import com.powerlifting.assistant.domain.model.ProgramSchedule
import com.powerlifting.assistant.domain.model.ProgramWorkout
import com.powerlifting.assistant.domain.model.TrainingCalendar
import com.powerlifting.assistant.domain.model.TrainingProgram
import java.time.LocalDate

interface ProgramRepository {
    suspend fun getActive(): ActiveProgram
    suspend fun generate(
        startDate: LocalDate? = null,
        weeks: Int? = null,
        schedule: ProgramSchedule? = null
    ): TrainingProgram
    suspend fun calendar(from: String, to: String): TrainingCalendar
    suspend fun rescheduleWorkout(workoutId: String, newDate: LocalDate): ProgramWorkout
    suspend fun skipWorkout(workoutId: String)
}
