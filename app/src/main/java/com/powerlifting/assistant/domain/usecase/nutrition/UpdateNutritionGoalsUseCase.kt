package com.powerlifting.assistant.domain.usecase.nutrition

import com.powerlifting.assistant.domain.repository.NutritionRepository
import javax.inject.Inject

class UpdateNutritionGoalsUseCase @Inject constructor(
    private val nutritionRepository: NutritionRepository
) {
    suspend operator fun invoke(caloriesGoal: Int, proteinGoalG: Int) {
        nutritionRepository.updateGoals(caloriesGoal, proteinGoalG)
    }
}
