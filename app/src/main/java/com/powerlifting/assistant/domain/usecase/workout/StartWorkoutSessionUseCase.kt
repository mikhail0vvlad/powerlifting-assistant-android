package com.powerlifting.assistant.domain.usecase.workout

import com.powerlifting.assistant.domain.model.StartSessionParams
import com.powerlifting.assistant.domain.model.WorkoutSessionStart
import com.powerlifting.assistant.domain.repository.WorkoutRepository
import javax.inject.Inject

class StartWorkoutSessionUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(params: StartSessionParams): WorkoutSessionStart =
        workoutRepository.startSession(params)
}
