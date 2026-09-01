package com.powerlifting.assistant.data.mapper

import com.powerlifting.assistant.data.api.ScheduleDto
import com.powerlifting.assistant.domain.model.ProgramSchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Юнит-тесты для маппинга расписания программы между DTO и доменной sealed-моделью.
 * Покрывает разбор дней недели/дат, отбраковку мусора и round-trip туда-обратно.
 */
class ProgramScheduleMapperTest {

    @Test
    fun `weekdays dto превращается в Weekdays с DayOfWeek`() {
        val dto = ScheduleDto(type = "weekdays", weekdays = listOf(1, 3, 5))

        val schedule = dto.toDomain()

        assertTrue(schedule is ProgramSchedule.Weekdays)
        assertEquals(
            setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            (schedule as ProgramSchedule.Weekdays).days
        )
    }

    @Test
    fun `weekdays отбрасывает значения вне диапазона 1_7`() {
        val dto = ScheduleDto(type = "weekdays", weekdays = listOf(0, 3, 8, 99))

        val schedule = dto.toDomain() as ProgramSchedule.Weekdays

        assertEquals(setOf(DayOfWeek.WEDNESDAY), schedule.days)
    }

    @Test
    fun `weekdays без валидных дней даёт null`() {
        val dto = ScheduleDto(type = "weekdays", weekdays = listOf(0, 8))
        assertNull(dto.toDomain())

        val empty = ScheduleDto(type = "weekdays", weekdays = null)
        assertNull(empty.toDomain())
    }

    @Test
    fun `dates dto превращается в Dates, мусорные даты пропускаются`() {
        val dto = ScheduleDto(
            type = "dates",
            dates = listOf("2026-06-08", "не-дата", "2026-06-10")
        )

        val schedule = dto.toDomain() as ProgramSchedule.Dates

        assertEquals(
            listOf(LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 10)),
            schedule.dates
        )
    }

    @Test
    fun `dates без валидных дат даёт null`() {
        val dto = ScheduleDto(type = "dates", dates = listOf("garbage", ""))
        assertNull(dto.toDomain())
    }

    @Test
    fun `неизвестный тип расписания даёт null`() {
        assertNull(ScheduleDto(type = "monthly").toDomain())
    }

    @Test
    fun `тип расписания нечувствителен к регистру`() {
        val dto = ScheduleDto(type = "WEEKDAYS", weekdays = listOf(2))
        assertTrue(dto.toDomain() is ProgramSchedule.Weekdays)
    }

    @Test
    fun `round-trip Weekdays сохраняет дни и сортирует их в DTO`() {
        val domain = ProgramSchedule.Weekdays(
            setOf(DayOfWeek.FRIDAY, DayOfWeek.MONDAY)
        )

        val dto = domain.toDto()

        assertEquals("weekdays", dto.type)
        assertEquals(listOf(1, 5), dto.weekdays) // отсортировано по возрастанию
        assertEquals(domain, dto.toDomain())
    }

    @Test
    fun `round-trip Dates сохраняет даты`() {
        val domain = ProgramSchedule.Dates(
            listOf(LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 10))
        )

        val dto = domain.toDto()

        assertEquals("dates", dto.type)
        assertEquals(listOf("2026-06-08", "2026-06-10"), dto.dates)
        assertEquals(domain, dto.toDomain())
    }
}
