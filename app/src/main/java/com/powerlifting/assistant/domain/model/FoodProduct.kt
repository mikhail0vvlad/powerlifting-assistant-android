package com.powerlifting.assistant.domain.model

import kotlinx.serialization.Serializable

/**
 * Базовый продукт из локального справочника. КБЖУ указаны на 100 г.
 */
@Serializable
data class FoodProduct(
    val id: String,
    val name: String,
    val calories: Int,
    val proteinG: Double,
    val fatG: Double,
    val carbsG: Double
)
