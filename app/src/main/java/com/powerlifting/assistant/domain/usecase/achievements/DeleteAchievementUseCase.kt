package com.powerlifting.assistant.domain.usecase.achievements

import com.powerlifting.assistant.domain.repository.AchievementsRepository
import javax.inject.Inject

class DeleteAchievementUseCase @Inject constructor(
    private val achievementsRepository: AchievementsRepository
) {
    suspend operator fun invoke(id: String) {
        achievementsRepository.delete(id)
    }
}
