package com.powerlifting.assistant.domain.repository

import com.powerlifting.assistant.domain.model.NutritionDay
import com.powerlifting.assistant.domain.model.NutritionEntry
import com.powerlifting.assistant.domain.model.NutritionGoals

interface NutritionRepository {
    suspend fun getToday(dateIso: String? = null): NutritionDay
    suspend fun updateGoals(caloriesGoal: Int, proteinGoalG: Int): NutritionGoals
    suspend fun addEntry(title: String, calories: Int, proteinG: Int): NutritionEntry
    suspend fun deleteEntry(id: String)
}
