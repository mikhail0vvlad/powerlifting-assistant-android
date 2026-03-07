package com.powerlifting.assistant.domain.usecase.program

import com.powerlifting.assistant.domain.repository.ProgramRepository
import javax.inject.Inject

class SkipWorkoutUseCase @Inject constructor(
    private val programRepository: ProgramRepository
) {
    suspend operator fun invoke(workoutId: String) =
        programRepository.skipWorkout(workoutId)
}
