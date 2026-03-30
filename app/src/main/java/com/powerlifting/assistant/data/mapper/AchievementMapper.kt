package com.powerlifting.assistant.data.mapper

import com.powerlifting.assistant.data.api.AchievementDto
import com.powerlifting.assistant.domain.model.Achievement

fun AchievementDto.toDomain() = Achievement(
    id = id,
    createdAtIso = createdAtIso,
    note = note,
    photoUrl = photoUrl
)
