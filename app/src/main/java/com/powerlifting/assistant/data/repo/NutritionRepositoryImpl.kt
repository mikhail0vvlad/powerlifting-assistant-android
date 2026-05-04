package com.powerlifting.assistant.data.repo

import com.powerlifting.assistant.data.api.CreateNutritionEntryRequest
import com.powerlifting.assistant.data.api.PowerliftingApi
import com.powerlifting.assistant.data.api.UpdateNutritionGoalsRequest
import com.powerlifting.assistant.data.auth.FirebaseTokenProvider
import com.powerlifting.assistant.data.cache.AppCache
import com.powerlifting.assistant.data.mapper.toDomain
import com.powerlifting.assistant.domain.model.NutritionDay
import com.powerlifting.assistant.domain.model.NutritionEntry
import com.powerlifting.assistant.domain.model.NutritionGoals
import com.powerlifting.assistant.domain.repository.NutritionRepository
import javax.inject.Inject

class NutritionRepositoryImpl @Inject constructor(
    private val api: PowerliftingApi,
    private val tokenProvider: FirebaseTokenProvider,
    private val cache: AppCache
) : NutritionRepository {

    override suspend fun getToday(dateIso: String?): NutritionDay {
        cache.syncUser()
        val key = dateIso ?: TODAY_KEY
        cache.nutritionToday.get(key)?.let { return it }
        val auth = tokenProvider.bearerToken()
        val fresh = api.getNutritionToday(auth, dateIso).toDomain()
        cache.nutritionToday.put(key, fresh)
        return fresh
    }

    override suspend fun updateGoals(caloriesGoal: Int, proteinGoalG: Int): NutritionGoals {
        cache.syncUser()
        val auth = tokenProvider.bearerToken(true)
        val result = api.updateNutritionGoals(
            auth,
            UpdateNutritionGoalsRequest(caloriesGoal, proteinGoalG)
        ).toDomain()
        cache.afterNutritionChange()
        return result
    }

    override suspend fun addEntry(title: String, calories: Int, proteinG: Int): NutritionEntry {
        cache.syncUser()
        val auth = tokenProvider.bearerToken(true)
        val result = api.createNutritionEntry(
            auth,
            CreateNutritionEntryRequest(title, calories, proteinG)
        ).toDomain()
        cache.afterNutritionChange()
        return result
    }

    override suspend fun deleteEntry(id: String) {
        cache.syncUser()
        val auth = tokenProvider.bearerToken(true)
        api.deleteNutritionEntry(auth, id)
        cache.afterNutritionChange()
    }

    private companion object {
        const val TODAY_KEY = "today"
    }
}
