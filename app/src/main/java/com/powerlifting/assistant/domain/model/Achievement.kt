package com.powerlifting.assistant.domain.model

data class Achievement(
    val id: String,
    val createdAtIso: String,
    val note: String,
    val photoUrl: String? = null
)
