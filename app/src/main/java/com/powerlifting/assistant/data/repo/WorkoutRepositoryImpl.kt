package com.powerlifting.assistant.data.repo

import com.powerlifting.assistant.data.api.AddWorkoutSetsRequest
import com.powerlifting.assistant.data.api.FinishWorkoutWithRatingRequest
import com.powerlifting.assistant.data.api.PowerliftingApi
import com.powerlifting.assistant.data.auth.FirebaseTokenProvider
import com.powerlifting.assistant.data.cache.AppCache
import com.powerlifting.assistant.data.mapper.toDomain
import com.powerlifting.assistant.data.mapper.toDto
import com.powerlifting.assistant.data.mapper.toRequest
import com.powerlifting.assistant.domain.model.StartSessionParams
import com.powerlifting.assistant.domain.model.WorkoutHistoryItem
import com.powerlifting.assistant.domain.model.WorkoutSessionDetail
import com.powerlifting.assistant.domain.model.WorkoutSessionStart
import com.powerlifting.assistant.domain.model.WorkoutSet
import com.powerlifting.assistant.domain.repository.WorkoutRepository
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val api: PowerliftingApi,
    private val tokenProvider: FirebaseTokenProvider,
    private val cache: AppCache
) : WorkoutRepository {

    override suspend fun startSession(params: StartSessionParams): WorkoutSessionStart {
        cache.syncUser()
        val auth = tokenProvider.bearerToken(true)
        val result = api.startWorkoutSession(auth, params.toRequest()).toDomain()
        cache.afterWorkoutChange()
        return result
    }

    override suspend fun getSessionDetail(sessionId: String): WorkoutSessionDetail {
        cache.syncUser()
        cache.sessionDetail.get(sessionId)?.let { return it }
        val auth = tokenProvider.bearerToken()
        val fresh = api.getWorkoutSessionDetail(auth, sessionId).toDomain()
        cache.sessionDetail.put(sessionId, fresh)
        return fresh
    }

    override suspend fun addSets(sessionId: String, sets: List<WorkoutSet>) {
        cache.syncUser()
        val auth = tokenProvider.bearerToken(true)
        api.addWorkoutSets(auth, sessionId, AddWorkoutSetsRequest(sets.map { it.toDto() }))
        cache.afterWorkoutChange(sessionId)
    }

    override suspend fun finishSession(sessionId: String, durationSec: Int, wellbeingRating: Int?) {
        cache.syncUser()
        val auth = tokenProvider.bearerToken(true)
        api.finishWorkoutSession(
            auth,
            sessionId,
            FinishWorkoutWithRatingRequest(durationSec, wellbeingRating)
        )
        cache.afterWorkoutChange(sessionId)
    }

    override suspend fun getHistory(limit: Int): List<WorkoutHistoryItem> {
        cache.syncUser()
        cache.workoutHistory.get(limit)?.let { return it }
        val auth = tokenProvider.bearerToken()
        val fresh = api.getWorkoutHistory(auth, limit).sessions.map { it.toDomain() }
        cache.workoutHistory.put(limit, fresh)
        return fresh
    }

    override suspend fun deleteSession(sessionId: String) {
        cache.syncUser()
        val auth = tokenProvider.bearerToken(true)
        api.deleteWorkoutSession(auth, sessionId)
        cache.afterWorkoutChange(sessionId)
    }
}
