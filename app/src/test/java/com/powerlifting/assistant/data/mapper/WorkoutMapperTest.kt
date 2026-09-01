package com.powerlifting.assistant.data.mapper

import com.powerlifting.assistant.data.api.ProgramExerciseDto
import com.powerlifting.assistant.data.api.WorkoutHistoryItemDto
import com.powerlifting.assistant.data.api.WorkoutSessionDetailResponse
import com.powerlifting.assistant.data.api.WorkoutSessionResponse
import com.powerlifting.assistant.data.api.WorkoutSetDto
import com.powerlifting.assistant.domain.model.StartSessionParams
import com.powerlifting.assistant.domain.model.WorkoutSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Юнит-тесты для маппинга домена тренировки между DTO и доменными моделями.
 * Контракт клиента и сервера зеркальны — эти тесты ловят рассинхрон полей.
 */
class WorkoutMapperTest {

    @Test
    fun `WorkoutSet round-trip сохраняет все поля`() {
        val set = WorkoutSet(
            exerciseName = "Присед",
            setNumber = 2,
            weightKg = 142.5,
            reps = 5,
            rpe = 8.5
        )

        val restored = set.toDto().toDomain()

        assertEquals(set, restored)
    }

    @Test
    fun `WorkoutSet с null rpe маппится корректно`() {
        val dto = WorkoutSetDto("Жим", 1, 100.0, 5, rpe = null)
        val domain = dto.toDomain()
        assertNull(domain.rpe)
        assertEquals("Жим", domain.exerciseName)
    }

    @Test
    fun `StartSessionParams превращается в request с теми же значениями`() {
        val params = StartSessionParams(
            programWorkoutId = "w1",
            sleepHours = 7.5,
            wellbeing = 4,
            fatigue = 2,
            soreness = 3
        )

        val request = params.toRequest()

        assertEquals("w1", request.programWorkoutId)
        assertEquals(7.5, request.sleepHours!!, 0.0001)
        assertEquals(4, request.wellbeing)
        assertEquals(2, request.fatigue)
        assertEquals(3, request.soreness)
    }

    @Test
    fun `WorkoutSessionResponse маппится в WorkoutSessionStart`() {
        val response = WorkoutSessionResponse(sessionId = "s1", recommendation = "Отдохни")
        val domain = response.toDomain()
        assertEquals("s1", domain.sessionId)
        assertEquals("Отдохни", domain.recommendation)
    }

    @Test
    fun `WorkoutSessionDetailResponse маппит вложенные упражнения и подходы`() {
        val response = WorkoutSessionDetailResponse(
            sessionId = "s1",
            programWorkoutId = "w1",
            recommendation = null,
            exercises = listOf(
                ProgramExerciseDto(
                    id = "e1", exerciseName = "Присед", orderIndex = 0,
                    sets = 5, reps = "5", percent1rm = 0.8, liftType = "squat"
                )
            ),
            loggedSets = listOf(WorkoutSetDto("Присед", 1, 160.0, 5, rpe = 8.0))
        )

        val domain = response.toDomain()

        assertEquals("s1", domain.sessionId)
        assertEquals(1, domain.exercises.size)
        assertEquals("squat", domain.exercises.single().liftType)
        assertEquals(1, domain.loggedSets.size)
        assertEquals(160.0, domain.loggedSets.single().weightKg, 0.0001)
    }

    @Test
    fun `WorkoutSessionDetailResponse с пустыми списками по умолчанию`() {
        val domain = WorkoutSessionDetailResponse(sessionId = "s1").toDomain()
        assertEquals(emptyList<Any>(), domain.exercises)
        assertEquals(emptyList<Any>(), domain.loggedSets)
    }

    @Test
    fun `WorkoutHistoryItemDto маппится со всеми полями`() {
        val dto = WorkoutHistoryItemDto(
            sessionId = "s1",
            date = "2026-06-08",
            durationSec = 3600,
            workoutTitle = "День A",
            wellbeingRating = 5,
            setsCount = 12
        )

        val domain = dto.toDomain()

        assertEquals("s1", domain.sessionId)
        assertEquals("2026-06-08", domain.date)
        assertEquals(3600, domain.durationSec)
        assertEquals("День A", domain.workoutTitle)
        assertEquals(5, domain.wellbeingRating)
        assertEquals(12, domain.setsCount)
    }
}
