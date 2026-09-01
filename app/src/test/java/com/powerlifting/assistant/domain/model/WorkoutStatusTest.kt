package com.powerlifting.assistant.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Юнит-тесты для разбора статуса тренировки [WorkoutStatus.parse].
 * Правило: неизвестное/пустое значение трактуется как PLANNED, регистр игнорируется.
 */
class WorkoutStatusTest {

    @Test
    fun `известные значения разбираются по проводному имени`() {
        assertEquals(WorkoutStatus.PLANNED, WorkoutStatus.parse("planned"))
        assertEquals(WorkoutStatus.COMPLETED, WorkoutStatus.parse("completed"))
        assertEquals(WorkoutStatus.MISSED, WorkoutStatus.parse("missed"))
        assertEquals(WorkoutStatus.RESCHEDULED, WorkoutStatus.parse("rescheduled"))
    }

    @Test
    fun `регистр игнорируется`() {
        assertEquals(WorkoutStatus.COMPLETED, WorkoutStatus.parse("COMPLETED"))
        assertEquals(WorkoutStatus.MISSED, WorkoutStatus.parse("Missed"))
    }

    @Test
    fun `null превращается в PLANNED`() {
        assertEquals(WorkoutStatus.PLANNED, WorkoutStatus.parse(null))
    }

    @Test
    fun `неизвестная строка превращается в PLANNED`() {
        assertEquals(WorkoutStatus.PLANNED, WorkoutStatus.parse("whatever"))
        assertEquals(WorkoutStatus.PLANNED, WorkoutStatus.parse(""))
    }

    @Test
    fun `statusEnum у ProgramWorkout и CalendarDay использует parse`() {
        val workout = ProgramWorkout(
            id = "1", date = "2026-06-08", title = "День A",
            status = "completed", exercises = emptyList()
        )
        val day = CalendarDay(date = "2026-06-08", title = "День A", status = "garbage")

        assertEquals(WorkoutStatus.COMPLETED, workout.statusEnum)
        assertEquals(WorkoutStatus.PLANNED, day.statusEnum) // мусор -> PLANNED
    }
}
