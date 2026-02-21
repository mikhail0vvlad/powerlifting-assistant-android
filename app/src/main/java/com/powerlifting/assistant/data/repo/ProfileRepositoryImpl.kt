package com.powerlifting.assistant.data.repo

import com.powerlifting.assistant.data.api.PowerliftingApi
import com.powerlifting.assistant.data.auth.FirebaseTokenProvider
import com.powerlifting.assistant.data.cache.AppCache
import com.powerlifting.assistant.data.mapper.toDomain
import com.powerlifting.assistant.data.mapper.toRequest
import com.powerlifting.assistant.domain.model.ProfileSummary
import com.powerlifting.assistant.domain.model.ProfileUpdate
import com.powerlifting.assistant.domain.model.UserProfile
import com.powerlifting.assistant.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val api: PowerliftingApi,
    private val tokenProvider: FirebaseTokenProvider,
    private val cache: AppCache
) : ProfileRepository {

    override suspend fun getProfile(): ProfileSummary {
        cache.syncUser()
        cache.profile.get(Unit)?.let { return it }
        val auth = tokenProvider.bearerToken()
        val fresh = api.getProfile(auth).toDomain()
        cache.profile.put(Unit, fresh)
        return fresh
    }

    override suspend fun updateProfile(update: ProfileUpdate): UserProfile {
        cache.syncUser()
        val auth = tokenProvider.bearerToken(true)
        val result = api.updateProfile(auth, update.toRequest()).toDomain()
        cache.afterProfileChange()
        return result
    }
}
