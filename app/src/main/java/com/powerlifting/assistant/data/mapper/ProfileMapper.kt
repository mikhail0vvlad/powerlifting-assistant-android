package com.powerlifting.assistant.data.mapper

import com.powerlifting.assistant.data.api.MeResponse
import com.powerlifting.assistant.data.api.NutritionGoalsDto
import com.powerlifting.assistant.data.api.ProfileResponse
import com.powerlifting.assistant.data.api.StatsDto
import com.powerlifting.assistant.data.api.UpdateProfileRequest
import com.powerlifting.assistant.data.api.UserProfileDto
import com.powerlifting.assistant.domain.model.NutritionGoals
import com.powerlifting.assistant.domain.model.ProfileSummary
import com.powerlifting.assistant.domain.model.ProfileUpdate
import com.powerlifting.assistant.domain.model.User
import com.powerlifting.assistant.domain.model.UserProfile
import com.powerlifting.assistant.domain.model.UserStats

fun MeResponse.toDomain() = User(
    userId = userId,
    firebaseUid = firebaseUid,
    email = email,
    displayName = displayName
)

fun UserProfileDto.toDomain() = UserProfile(
    heightCm = heightCm,
    weightKg = weightKg,
    bench1rm = bench1rm,
    squat1rm = squat1rm,
    deadlift1rm = deadlift1rm
)

fun NutritionGoalsDto.toDomain() = NutritionGoals(
    caloriesGoal = caloriesGoal,
    proteinGoalG = proteinGoalG
)

fun StatsDto.toDomain() = UserStats(
    achievementsCount = achievementsCount,
    caloriesToday = caloriesToday,
    proteinToday = proteinToday
)

fun ProfileResponse.toDomain() = ProfileSummary(
    user = me.toDomain(),
    profile = profile.toDomain(),
    nutritionGoals = nutritionGoals.toDomain(),
    stats = stats.toDomain()
)

fun ProfileUpdate.toRequest() = UpdateProfileRequest(
    heightCm = heightCm,
    weightKg = weightKg,
    bench1rm = bench1rm,
    squat1rm = squat1rm,
    deadlift1rm = deadlift1rm
)
