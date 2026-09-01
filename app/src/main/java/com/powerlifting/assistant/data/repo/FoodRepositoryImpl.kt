package com.powerlifting.assistant.data.repo

import com.powerlifting.assistant.data.local.AppPreferences
import com.powerlifting.assistant.domain.model.FoodProduct
import com.powerlifting.assistant.domain.repository.FoodRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val prefs: AppPreferences
) : FoodRepository {

    override suspend fun search(query: String): List<FoodProduct> {
        // Небольшая задержка, чтобы был виден ProgressBar при поиске.
        delay(350)
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return CATALOG.filter { it.name.contains(q, ignoreCase = true) }
    }

    override fun history(): Flow<List<FoodProduct>> = prefs.searchHistory

    override suspend fun addToHistory(product: FoodProduct) = prefs.addToHistory(product)

    override suspend fun clearHistory() = prefs.clearHistory()

    private companion object {
        // Локальное хранилище базовых продуктов с КБЖУ (на 100 г).
        val CATALOG = listOf(
            FoodProduct(
                id = "eggs",
                name = "Яйца",
                calories = 157,
                proteinG = 12.7,
                fatG = 11.5,
                carbsG = 0.7
            ),
            FoodProduct(
                id = "chicken_breast",
                name = "Куриная грудка",
                calories = 137,
                proteinG = 29.8,
                fatG = 1.8,
                carbsG = 0.5
            ),
            FoodProduct(
                id = "rice",
                name = "Рис",
                calories = 116,
                proteinG = 2.2,
                fatG = 0.5,
                carbsG = 24.9
            )
        )
    }
}
