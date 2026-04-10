package com.powerlifting.assistant.domain.model

data class NutritionEntry(
    val id: String,
    val title: String,
    val eatenAtIso: String,
    val calories: Int,
    val proteinG: Int
)

data class NutritionTotals(
    val calories: Int,
    val proteinG: Int
)

data class NutritionDay(
    val date: String,
    val totals: NutritionTotals,
    val goals: NutritionGoals,
    val entries: List<NutritionEntry>
)
