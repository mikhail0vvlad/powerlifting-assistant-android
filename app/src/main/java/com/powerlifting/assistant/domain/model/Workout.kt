package com.powerlifting.assistant.domain.model

data class WorkoutSet(
    val exerciseName: String,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val rpe: Double? = null
)

data class StartSessionParams(
    val programWorkoutId: String? = null,
    val sleepHours: Double? = null,
    val wellbeing: Int? = null,
    val fatigue: Int? = null,
    val soreness: Int? = null
)

data class WorkoutSessionStart(
    val sessionId: String,
    val recommendation: String? = null
)

data class WorkoutSessionDetail(
    val sessionId: String,
    val programWorkoutId: String? = null,
    val recommendation: String? = null,
    val exercises: List<ProgramExercise> = emptyList(),
    val loggedSets: List<WorkoutSet> = emptyList()
)

data class WorkoutHistoryItem(
    val sessionId: String,
    val date: String,
    val durationSec: Int? = null,
    val workoutTitle: String? = null,
    val wellbeingRating: Int? = null,
    val setsCount: Int = 0
)

data class ExerciseGroup(
    val name: String,
    val liftType: String,
    val setGroups: List<SetGroupInfo>,
    val isMain: Boolean
)

data class SetGroupInfo(
    val percent1rm: Double?,
    val targetReps: Int,
    val targetSets: Int,
    val weightKg: Double?,
    val completedSets: Int = 0
) {
    val totalSets: Int get() = targetSets
    val allCompleted: Boolean get() = completedSets >= totalSets
}
