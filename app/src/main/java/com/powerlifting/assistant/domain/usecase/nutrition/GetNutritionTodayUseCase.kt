package com.powerlifting.assistant.domain.usecase.nutrition

import com.powerlifting.assistant.domain.model.NutritionDay
import com.powerlifting.assistant.domain.repository.NutritionRepository
import javax.inject.Inject

class GetNutritionTodayUseCase @Inject constructor(
    private val nutritionRepository: NutritionRepository
) {
    suspend operator fun invoke(dateIso: String? = null): NutritionDay =
        nutritionRepository.getToday(dateIso)
}
