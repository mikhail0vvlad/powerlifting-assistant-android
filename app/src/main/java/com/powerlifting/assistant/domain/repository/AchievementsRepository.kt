package com.powerlifting.assistant.domain.repository

import com.powerlifting.assistant.domain.model.Achievement

interface AchievementsRepository {
    suspend fun list(): List<Achievement>
    suspend fun create(note: String, photoUrl: String? = null): Achievement
    suspend fun delete(id: String)
}
