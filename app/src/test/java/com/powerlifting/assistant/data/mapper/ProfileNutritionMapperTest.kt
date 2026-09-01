package com.powerlifting.assistant.data.mapper

import com.powerlifting.assistant.data.api.AchievementDto
import com.powerlifting.assistant.data.api.MeResponse
import com.powerlifting.assistant.data.api.NutritionEntryDto
import com.powerlifting.assistant.data.api.NutritionGoalsDto
import com.powerlifting.assistant.data.api.NutritionTodayResponse
import com.powerlifting.assistant.data.api.NutritionTotalsDto
import com.powerlifting.assistant.data.api.ProfileResponse
import com.powerlifting.assistant.data.api.StatsDto
import com.powerlifting.assistant.data.api.UserProfileDto
import com.powerlifting.assistant.domain.model.ProfileUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Юнит-тесты для мапперов профиля, питания и достижений (DTO → domain).
 * Проверяют, что все поля контракта переносятся без потерь.
 */
class ProfileNutritionMapperTest {

    @Test
    fun `ProfileResponse собирает полный ProfileSummary`() {
        val response = ProfileResponse(
            me = MeResponse("u1", "fb-uid", "a@b.c", "Вася"),
            profile = UserProfileDto(
                heightCm = 180, weightKg = 90.0,
                bench1rm = 100.0, squat1rm = 200.0, deadlift1rm = 250.0
            ),
            nutritionGoals = NutritionGoalsDto(caloriesGoal = 3000, proteinGoalG = 180),
            stats = StatsDto(achievementsCount = 3, caloriesToday = 1500, proteinToday = 90)
        )

        val summary = response.toDomain()

        assertEquals("u1", summary.user.userId)
        assertEquals("Вася", summary.user.displayName)
        assertEquals(180, summary.profile.heightCm)
        assertEquals(250.0, summary.profile.deadlift1rm!!, 0.0001)
        assertEquals(3000, summary.nutritionGoals.caloriesGoal)
        assertEquals(3, summary.stats.achievementsCount)
        assertEquals(1500, summary.stats.caloriesToday)
    }

    @Test
    fun `UserProfileDto с null максимумами не теряет nullability`() {
        val profile = UserProfileDto(heightCm = 170).toDomain()
        assertEquals(170, profile.heightCm)
        assertNull(profile.bench1rm)
        assertNull(profile.weightKg)
    }

    @Test
    fun `ProfileUpdate превращается в request один в один`() {
        val update = ProfileUpdate(
            heightCm = 175, weightKg = 80.0,
            bench1rm = 90.0, squat1rm = 150.0, deadlift1rm = 200.0
        )

        val request = update.toRequest()

        assertEquals(175, request.heightCm)
        assertEquals(80.0, request.weightKg!!, 0.0001)
        assertEquals(90.0, request.bench1rm!!, 0.0001)
        assertEquals(150.0, request.squat1rm!!, 0.0001)
        assertEquals(200.0, request.deadlift1rm!!, 0.0001)
    }

    @Test
    fun `NutritionTodayResponse маппит итоги, цели и записи`() {
        val response = NutritionTodayResponse(
            date = "2026-06-08",
            totals = NutritionTotalsDto(calories = 1200, proteinG = 80),
            goals = NutritionGoalsDto(caloriesGoal = 3000, proteinGoalG = 180),
            entries = listOf(
                NutritionEntryDto(
                    id = "n1", title = "Овсянка", eatenAtIso = "2026-06-08T08:00:00Z",
                    calories = 350, proteinG = 12
                )
            )
        )

        val day = response.toDomain()

        assertEquals("2026-06-08", day.date)
        assertEquals(1200, day.totals.calories)
        assertEquals(80, day.totals.proteinG)
        assertEquals(3000, day.goals.caloriesGoal)
        assertEquals(1, day.entries.size)
        assertEquals("Овсянка", day.entries.single().title)
        assertEquals(350, day.entries.single().calories)
    }

    @Test
    fun `AchievementDto маппится, photoUrl сохраняет null`() {
        val withPhoto = AchievementDto(
            id = "a1", createdAtIso = "2026-06-08T10:00:00Z",
            note = "Новый рекорд", photoUrl = "https://x/y.jpg"
        ).toDomain()
        val withoutPhoto = AchievementDto(
            id = "a2", createdAtIso = "2026-06-08T10:00:00Z", note = "PR"
        ).toDomain()

        assertEquals("Новый рекорд", withPhoto.note)
        assertEquals("https://x/y.jpg", withPhoto.photoUrl)
        assertNull(withoutPhoto.photoUrl)
    }
}
