package com.powerlifting.assistant.domain.usecase.workout

import com.powerlifting.assistant.domain.model.WorkoutSet
import com.powerlifting.assistant.domain.repository.WorkoutRepository
import javax.inject.Inject

class AddWorkoutSetsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: String, sets: List<WorkoutSet>) {
        workoutRepository.addSets(sessionId, sets)
    }
}
