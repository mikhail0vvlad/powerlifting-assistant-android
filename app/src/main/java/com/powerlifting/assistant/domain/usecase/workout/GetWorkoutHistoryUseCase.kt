package com.powerlifting.assistant.domain.usecase.workout

import com.powerlifting.assistant.domain.model.WorkoutHistoryItem
import com.powerlifting.assistant.domain.repository.WorkoutRepository
import javax.inject.Inject

class GetWorkoutHistoryUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(limit: Int = 30): List<WorkoutHistoryItem> =
        workoutRepository.getHistory(limit)
}
