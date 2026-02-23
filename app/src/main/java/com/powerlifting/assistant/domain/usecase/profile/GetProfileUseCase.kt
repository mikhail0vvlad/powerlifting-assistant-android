package com.powerlifting.assistant.domain.usecase.profile

import com.powerlifting.assistant.domain.model.ProfileSummary
import com.powerlifting.assistant.domain.repository.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): ProfileSummary = profileRepository.getProfile()
}
