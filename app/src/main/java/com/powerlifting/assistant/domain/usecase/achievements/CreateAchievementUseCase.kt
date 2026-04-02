package com.powerlifting.assistant.domain.usecase.achievements

import com.powerlifting.assistant.domain.repository.AchievementsRepository
import javax.inject.Inject

class CreateAchievementUseCase @Inject constructor(
    private val achievementsRepository: AchievementsRepository
) {
    suspend operator fun invoke(note: String, photoUrl: String? = null) {
        achievementsRepository.create(note, photoUrl)
    }
}
