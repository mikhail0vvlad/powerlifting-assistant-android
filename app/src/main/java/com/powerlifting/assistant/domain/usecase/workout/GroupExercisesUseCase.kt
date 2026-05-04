package com.powerlifting.assistant.domain.usecase.workout

import com.powerlifting.assistant.domain.model.ExerciseGroup
import com.powerlifting.assistant.domain.model.ProgramExercise
import com.powerlifting.assistant.domain.model.SetGroupInfo
import com.powerlifting.assistant.domain.model.UserProfile
import javax.inject.Inject
import kotlin.math.round

class GroupExercisesUseCase @Inject constructor() {

    operator fun invoke(
        exercises: List<ProgramExercise>,
        profile: UserProfile
    ): List<ExerciseGroup> {
        val bench = profile.bench1rm ?: 0.0
        val squat = profile.squat1rm ?: 0.0
        val deadlift = profile.deadlift1rm ?: 0.0

        return exercises.groupBy { it.exerciseName }.map { (name, exList) ->
            val liftType = exList.first().liftType
            val isMain = liftType in MAIN_LIFTS

            val rm = when (liftType) {
                "squat" -> squat
                "bench" -> bench
                "deadlift" -> deadlift
                else -> 0.0
            }

            val setGroups = exList.sortedBy { it.orderIndex }.map { ex ->
                val reps = ex.reps.toIntOrNull()
                    ?: ex.reps.split("-").firstOrNull()?.toIntOrNull()
                    ?: DEFAULT_REPS
                val weight = if (ex.percent1rm != null && rm > 0) {
                    roundToPlate(rm * ex.percent1rm)
                } else null

                SetGroupInfo(
                    percent1rm = ex.percent1rm,
                    targetReps = reps,
                    targetSets = ex.sets,
                    weightKg = weight
                )
            }

            ExerciseGroup(
                name = name,
                liftType = liftType,
                isMain = isMain,
                setGroups = setGroups
            )
        }
    }

    private fun roundToPlate(kg: Double): Double = round(kg / PLATE_STEP) * PLATE_STEP

    companion object {
        private val MAIN_LIFTS = setOf("squat", "bench", "deadlift")
        private const val DEFAULT_REPS = 8
        private const val PLATE_STEP = 2.5
    }
}
