package com.powerlifting.assistant.data.mapper

import com.powerlifting.assistant.data.api.StartWorkoutSessionRequest
import com.powerlifting.assistant.data.api.WorkoutHistoryItemDto
import com.powerlifting.assistant.data.api.WorkoutSessionDetailResponse
import com.powerlifting.assistant.data.api.WorkoutSessionResponse
import com.powerlifting.assistant.data.api.WorkoutSetDto
import com.powerlifting.assistant.domain.model.StartSessionParams
import com.powerlifting.assistant.domain.model.WorkoutHistoryItem
import com.powerlifting.assistant.domain.model.WorkoutSessionDetail
import com.powerlifting.assistant.domain.model.WorkoutSessionStart
import com.powerlifting.assistant.domain.model.WorkoutSet

fun WorkoutSetDto.toDomain() = WorkoutSet(
    exerciseName = exerciseName,
    setNumber = setNumber,
    weightKg = weightKg,
    reps = reps,
    rpe = rpe
)

fun WorkoutSet.toDto() = WorkoutSetDto(
    exerciseName = exerciseName,
    setNumber = setNumber,
    weightKg = weightKg,
    reps = reps,
    rpe = rpe
)

fun StartSessionParams.toRequest() = StartWorkoutSessionRequest(
    programWorkoutId = programWorkoutId,
    sleepHours = sleepHours,
    wellbeing = wellbeing,
    fatigue = fatigue,
    soreness = soreness
)

fun WorkoutSessionResponse.toDomain() = WorkoutSessionStart(
    sessionId = sessionId,
    recommendation = recommendation
)

fun WorkoutSessionDetailResponse.toDomain() = WorkoutSessionDetail(
    sessionId = sessionId,
    programWorkoutId = programWorkoutId,
    recommendation = recommendation,
    exercises = exercises.map { it.toDomain() },
    loggedSets = loggedSets.map { it.toDomain() }
)

fun WorkoutHistoryItemDto.toDomain() = WorkoutHistoryItem(
    sessionId = sessionId,
    date = date,
    durationSec = durationSec,
    workoutTitle = workoutTitle,
    wellbeingRating = wellbeingRating,
    setsCount = setsCount
)
