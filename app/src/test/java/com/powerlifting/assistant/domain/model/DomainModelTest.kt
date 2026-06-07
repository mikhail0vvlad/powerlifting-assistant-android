package com.powerlifting.assistant.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Юнит-тесты для вычисляемых свойств доменных моделей:
 * [UserProfile.hasAllMaxes] и [SetGroupInfo.allCompleted]/[SetGroupInfo.totalSets].
 */
class DomainModelTest {

    @Test
    fun `hasAllMaxes истинно только когда заданы все три максимума`() {
        val full = UserProfile(bench1rm = 100.0, squat1rm = 200.0, deadlift1rm = 250.0)
        assertTrue(full.hasAllMaxes)
    }

    @Test
    fun `hasAllMaxes ложно если хоть один максимум отсутствует`() {
        assertFalse(UserProfile(bench1rm = 100.0, squat1rm = 200.0).hasAllMaxes)
        assertFalse(UserProfile().hasAllMaxes)
    }

    @Test
    fun `allCompleted истинно когда выполнено не меньше целевых подходов`() {
        val group = SetGroupInfo(
            percent1rm = 0.8, targetReps = 5, targetSets = 3,
            weightKg = 100.0, completedSets = 3
        )
        assertTrue(group.allCompleted)
        assertEquals3(group.totalSets)
    }

    @Test
    fun `allCompleted истинно при перевыполнении`() {
        val group = SetGroupInfo(
            percent1rm = null, targetReps = 8, targetSets = 2,
            weightKg = null, completedSets = 5
        )
        assertTrue(group.allCompleted)
    }

    @Test
    fun `allCompleted ложно когда подходов меньше цели`() {
        val group = SetGroupInfo(
            percent1rm = 0.8, targetReps = 5, targetSets = 3,
            weightKg = 100.0, completedSets = 2
        )
        assertFalse(group.allCompleted)
    }

    @Test
    fun `completedSets по умолчанию ноль`() {
        val group = SetGroupInfo(
            percent1rm = 0.8, targetReps = 5, targetSets = 3, weightKg = 100.0
        )
        assertFalse(group.allCompleted)
    }

    private fun assertEquals3(actual: Int) = assertTrue(actual == 3)
}
