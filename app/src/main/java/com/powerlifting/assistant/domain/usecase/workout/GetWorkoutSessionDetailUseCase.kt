package com.powerlifting.assistant.domain.usecase.workout

import com.powerlifting.assistant.domain.model.WorkoutSessionDetail
import com.powerlifting.assistant.domain.repository.WorkoutRepository
import javax.inject.Inject

class GetWorkoutSessionDetailUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: String): WorkoutSessionDetail =
        workoutRepository.getSessionDetail(sessionId)
}
