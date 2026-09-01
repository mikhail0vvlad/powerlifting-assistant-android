package com.powerlifting.assistant.domain.usecase.workout

import com.powerlifting.assistant.domain.model.StartSessionParams
import com.powerlifting.assistant.domain.model.WorkoutHistoryItem
import com.powerlifting.assistant.domain.model.WorkoutSessionDetail
import com.powerlifting.assistant.domain.model.WorkoutSessionStart
import com.powerlifting.assistant.domain.model.WorkoutSet
import com.powerlifting.assistant.domain.repository.WorkoutRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Юнит-тест для [StartWorkoutSessionUseCase] на корутинах (`runTest`).
 * Use case — тонкая обёртка, поэтому проверяем, что параметры доходят до репозитория
 * без изменений и результат пробрасывается обратно. Репозиторий заменён ручным fake.
 */
class StartWorkoutSessionUseCaseTest {

    /** Ручной фейк: запоминает аргумент и отдаёт заранее заданный результат. */
    private class FakeWorkoutRepository(
        private val result: WorkoutSessionStart
    ) : WorkoutRepository {
        var lastParams: StartSessionParams? = null
        var startCalls = 0

        override suspend fun startSession(params: StartSessionParams): WorkoutSessionStart {
            startCalls++
            lastParams = params
            return result
        }

        // Остальные методы в этом тесте не нужны.
        override suspend fun getSessionDetail(sessionId: String): WorkoutSessionDetail =
            throw NotImplementedError()
        override suspend fun addSets(sessionId: String, sets: List<WorkoutSet>) =
            throw NotImplementedError()
        override suspend fun finishSession(sessionId: String, durationSec: Int, wellbeingRating: Int?) =
            throw NotImplementedError()
        override suspend fun getHistory(limit: Int): List<WorkoutHistoryItem> =
            throw NotImplementedError()
        override suspend fun deleteSession(sessionId: String) =
            throw NotImplementedError()
    }

    @Test
    fun `invoke делегирует в репозиторий и возвращает его результат`() = runTest {
        val expected = WorkoutSessionStart(sessionId = "s42", recommendation = "Лёгкая тренировка")
        val repo = FakeWorkoutRepository(expected)
        val useCase = StartWorkoutSessionUseCase(repo)
        val params = StartSessionParams(programWorkoutId = "w1", sleepHours = 6.0, wellbeing = 3)

        val actual = useCase(params)

        assertSame(expected, actual)           // результат проброшен как есть
        assertEquals(1, repo.startCalls)        // вызван ровно один раз
        assertEquals(params, repo.lastParams)   // параметры дошли без изменений
    }
}
