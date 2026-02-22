package com.powerlifting.assistant.domain.usecase.profile

import com.powerlifting.assistant.domain.model.ProfileSummary
import com.powerlifting.assistant.domain.model.ProfileUpdate
import com.powerlifting.assistant.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(update: ProfileUpdate): ProfileSummary {
        profileRepository.updateProfile(update)
        return profileRepository.getProfile()
    }
}
