package com.powerlifting.assistant.domain.usecase.program

import com.powerlifting.assistant.domain.model.ProgramWorkout
import com.powerlifting.assistant.domain.repository.ProgramRepository
import java.time.LocalDate
import javax.inject.Inject

class RescheduleWorkoutUseCase @Inject constructor(
    private val programRepository: ProgramRepository
) {
    suspend operator fun invoke(workoutId: String, newDate: LocalDate): ProgramWorkout =
        programRepository.rescheduleWorkout(workoutId, newDate)
}
