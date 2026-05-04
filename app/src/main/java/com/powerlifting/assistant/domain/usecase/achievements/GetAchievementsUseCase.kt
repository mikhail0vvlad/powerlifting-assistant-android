package com.powerlifting.assistant.domain.usecase.achievements

import com.powerlifting.assistant.domain.model.Achievement
import com.powerlifting.assistant.domain.repository.AchievementsRepository
import javax.inject.Inject

class GetAchievementsUseCase @Inject constructor(
    private val achievementsRepository: AchievementsRepository
) {
    suspend operator fun invoke(): List<Achievement> = achievementsRepository.list()
}
