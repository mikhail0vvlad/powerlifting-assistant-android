package com.powerlifting.assistant.domain.repository

import com.powerlifting.assistant.domain.model.StartSessionParams
import com.powerlifting.assistant.domain.model.WorkoutHistoryItem
import com.powerlifting.assistant.domain.model.WorkoutSessionDetail
import com.powerlifting.assistant.domain.model.WorkoutSessionStart
import com.powerlifting.assistant.domain.model.WorkoutSet

interface WorkoutRepository {
    suspend fun startSession(params: StartSessionParams): WorkoutSessionStart
    suspend fun getSessionDetail(sessionId: String): WorkoutSessionDetail
    suspend fun addSets(sessionId: String, sets: List<WorkoutSet>)
    suspend fun finishSession(sessionId: String, durationSec: Int, wellbeingRating: Int? = null)
    suspend fun getHistory(limit: Int = 30): List<WorkoutHistoryItem>
    suspend fun deleteSession(sessionId: String)
}
