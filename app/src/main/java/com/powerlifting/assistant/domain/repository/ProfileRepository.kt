package com.powerlifting.assistant.domain.repository

import com.powerlifting.assistant.domain.model.ProfileSummary
import com.powerlifting.assistant.domain.model.ProfileUpdate
import com.powerlifting.assistant.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getProfile(): ProfileSummary
    suspend fun updateProfile(update: ProfileUpdate): UserProfile
}
