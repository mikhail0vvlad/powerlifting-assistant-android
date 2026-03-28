package com.powerlifting.assistant.domain.usecase.workout

import com.powerlifting.assistant.domain.repository.WorkoutRepository
import javax.inject.Inject

class DeleteWorkoutSessionUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: String) =
        workoutRepository.deleteSession(sessionId)
}
