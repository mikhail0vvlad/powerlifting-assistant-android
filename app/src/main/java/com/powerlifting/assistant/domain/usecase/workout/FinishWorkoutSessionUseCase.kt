package com.powerlifting.assistant.domain.usecase.workout

import com.powerlifting.assistant.domain.repository.WorkoutRepository
import javax.inject.Inject

class FinishWorkoutSessionUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: String, durationSec: Int, wellbeingRating: Int? = null) {
        workoutRepository.finishSession(sessionId, durationSec, wellbeingRating)
    }
}
