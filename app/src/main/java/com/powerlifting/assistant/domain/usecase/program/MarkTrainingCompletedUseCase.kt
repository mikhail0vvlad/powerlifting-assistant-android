package com.powerlifting.assistant.domain.usecase.program

import com.powerlifting.assistant.domain.model.StartSessionParams
import com.powerlifting.assistant.domain.repository.WorkoutRepository
import com.powerlifting.assistant.domain.usecase.workout.FinishWorkoutSessionUseCase
import javax.inject.Inject

/**
 * Maps a "training day completed" intent to the existing finish-session pipeline:
 * starts a session against the program workout, then finishes it. Useful when
 * the user wants to log "yes, I did this" without ticking individual sets.
 */
class MarkTrainingCompletedUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val finishSession: FinishWorkoutSessionUseCase
) {
    suspend operator fun invoke(programWorkoutId: String, durationSec: Int = 0, wellbeingRating: Int? = null) {
        val started = workoutRepository.startSession(StartSessionParams(programWorkoutId = programWorkoutId))
        finishSession(started.sessionId, durationSec, wellbeingRating)
    }
}
