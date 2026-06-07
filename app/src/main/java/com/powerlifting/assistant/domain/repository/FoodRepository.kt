package com.powerlifting.assistant.domain.repository

import com.powerlifting.assistant.domain.model.FoodProduct
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    /** Поиск по локальному справочнику базовых продуктов. */
    suspend fun search(query: String): List<FoodProduct>

    /** История выбранных продуктов (новые — вверху). */
    fun history(): Flow<List<FoodProduct>>

    suspend fun addToHistory(product: FoodProduct)

    suspend fun clearHistory()
}
