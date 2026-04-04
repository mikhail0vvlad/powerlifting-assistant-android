package com.powerlifting.assistant.data.mapper

import com.powerlifting.assistant.data.api.NutritionEntryDto
import com.powerlifting.assistant.data.api.NutritionTodayResponse
import com.powerlifting.assistant.data.api.NutritionTotalsDto
import com.powerlifting.assistant.domain.model.NutritionDay
import com.powerlifting.assistant.domain.model.NutritionEntry
import com.powerlifting.assistant.domain.model.NutritionTotals

fun NutritionEntryDto.toDomain() = NutritionEntry(
    id = id,
    title = title,
    eatenAtIso = eatenAtIso,
    calories = calories,
    proteinG = proteinG
)

fun NutritionTotalsDto.toDomain() = NutritionTotals(
    calories = calories,
    proteinG = proteinG
)

fun NutritionTodayResponse.toDomain() = NutritionDay(
    date = date,
    totals = totals.toDomain(),
    goals = goals.toDomain(),
    entries = entries.map { it.toDomain() }
)
