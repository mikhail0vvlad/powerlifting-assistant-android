package com.powerlifting.assistant.domain.model

data class User(
    val userId: String,
    val firebaseUid: String,
    val email: String? = null,
    val displayName: String? = null
)

data class UserProfile(
    val heightCm: Int? = null,
    val weightKg: Double? = null,
    val bench1rm: Double? = null,
    val squat1rm: Double? = null,
    val deadlift1rm: Double? = null
) {
    val hasAllMaxes: Boolean
        get() = bench1rm != null && squat1rm != null && deadlift1rm != null
}

data class NutritionGoals(
    val caloriesGoal: Int,
    val proteinGoalG: Int
)

data class UserStats(
    val achievementsCount: Int,
    val caloriesToday: Int,
    val proteinToday: Int
)

data class ProfileSummary(
    val user: User,
    val profile: UserProfile,
    val nutritionGoals: NutritionGoals,
    val stats: UserStats
)

data class ProfileUpdate(
    val heightCm: Int? = null,
    val weightKg: Double? = null,
    val bench1rm: Double? = null,
    val squat1rm: Double? = null,
    val deadlift1rm: Double? = null
)
