package com.powerlifting.assistant.data.repo

import com.powerlifting.assistant.data.api.CreateAchievementRequest
import com.powerlifting.assistant.data.api.PowerliftingApi
import com.powerlifting.assistant.data.auth.FirebaseTokenProvider
import com.powerlifting.assistant.data.cache.AppCache
import com.powerlifting.assistant.data.mapper.toDomain
import com.powerlifting.assistant.domain.model.Achievement
import com.powerlifting.assistant.domain.repository.AchievementsRepository
import javax.inject.Inject

class AchievementsRepositoryImpl @Inject constructor(
    private val api: PowerliftingApi,
    private val tokenProvider: FirebaseTokenProvider,
    private val cache: AppCache
) : AchievementsRepository {

    override suspend fun list(): List<Achievement> {
        cache.syncUser()
        cache.achievements.get(Unit)?.let { return it }
        val auth = tokenProvider.bearerToken()
        val fresh = api.getAchievements(auth).map { it.toDomain() }
        cache.achievements.put(Unit, fresh)
        return fresh
    }

    override suspend fun create(note: String, photoUrl: String?): Achievement {
        cache.syncUser()
        val auth = tokenProvider.bearerToken(true)
        val result = api.createAchievement(auth, CreateAchievementRequest(note, photoUrl)).toDomain()
        cache.afterAchievementChange()
        return result
    }

    override suspend fun delete(id: String) {
        cache.syncUser()
        val auth = tokenProvider.bearerToken(true)
        api.deleteAchievement(auth, id)
        cache.afterAchievementChange()
    }
}
