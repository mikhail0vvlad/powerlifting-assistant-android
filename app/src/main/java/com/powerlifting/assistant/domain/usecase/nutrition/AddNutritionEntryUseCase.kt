package com.powerlifting.assistant.domain.usecase.nutrition

import com.powerlifting.assistant.domain.repository.NutritionRepository
import javax.inject.Inject

class AddNutritionEntryUseCase @Inject constructor(
    private val nutritionRepository: NutritionRepository
) {
    suspend operator fun invoke(title: String, calories: Int, proteinG: Int) {
        nutritionRepository.addEntry(title, calories, proteinG)
    }
}
