package com.powerlifting.assistant.domain.usecase.nutrition

import com.powerlifting.assistant.domain.repository.NutritionRepository
import javax.inject.Inject

class DeleteNutritionEntryUseCase @Inject constructor(
    private val nutritionRepository: NutritionRepository
) {
    suspend operator fun invoke(id: String) {
        nutritionRepository.deleteEntry(id)
    }
}
