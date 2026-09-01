package com.powerlifting.assistant.domain.usecase.workout

import com.powerlifting.assistant.domain.model.ProgramExercise
import com.powerlifting.assistant.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Юнит-тесты для [GroupExercisesUseCase] — самой нагруженной логикой клиента:
 * группировка упражнений, расчёт рабочего веса (1ПМ * percent, округление до блина 2.5кг),
 * парсинг диапазона повторов и пометка «основных» движений.
 */
class GroupExercisesUseCaseTest {

    private lateinit var useCase: GroupExercisesUseCase

    // Профиль с заполненными максимумами: присед 200, жим 100, тяга 250.
    private val profile = UserProfile(
        heightCm = 180,
        weightKg = 90.0,
        bench1rm = 100.0,
        squat1rm = 200.0,
        deadlift1rm = 250.0
    )

    @Before
    fun setUp() {
        useCase = GroupExercisesUseCase()
    }

    private fun exercise(
        name: String,
        liftType: String,
        order: Int = 0,
        sets: Int = 5,
        reps: String = "5",
        percent: Double? = null
    ) = ProgramExercise(
        id = "$name-$order",
        exerciseName = name,
        orderIndex = order,
        sets = sets,
        reps = reps,
        percent1rm = percent,
        liftType = liftType
    )

    @Test
    fun `вес считается как 1ПМ умноженный на процент`() {
        // 200 * 0.8 = 160 — уже кратно 2.5, округление ничего не меняет.
        val result = useCase(
            listOf(exercise("Присед", "squat", percent = 0.8)),
            profile
        )

        val setGroup = result.single().setGroups.single()
        assertEquals(160.0, setGroup.weightKg!!, 0.0001)
    }

    @Test
    fun `вес округляется до ближайшего блина 2_5 кг`() {
        // 100 * 0.83 = 83.0 -> ближайший шаг 2.5 = 82.5
        val result = useCase(
            listOf(exercise("Жим", "bench", percent = 0.83)),
            profile
        )

        assertEquals(82.5, result.single().setGroups.single().weightKg!!, 0.0001)
    }

    @Test
    fun `вес равен null когда нет процента`() {
        val result = useCase(
            listOf(exercise("Присед", "squat", percent = null)),
            profile
        )

        assertNull(result.single().setGroups.single().weightKg)
    }

    @Test
    fun `вес равен null когда максимум не задан`() {
        val emptyProfile = UserProfile()
        val result = useCase(
            listOf(exercise("Присед", "squat", percent = 0.8)),
            emptyProfile
        )

        assertNull(result.single().setGroups.single().weightKg)
    }

    @Test
    fun `диапазон повторов парсится как нижняя граница`() {
        // "5-8" -> 5
        val result = useCase(
            listOf(exercise("Присед", "squat", reps = "5-8")),
            profile
        )

        assertEquals(5, result.single().setGroups.single().targetReps)
    }

    @Test
    fun `некорректные повторы дают дефолт 8`() {
        val result = useCase(
            listOf(exercise("Растяжка", "accessory", reps = "AMRAP")),
            profile
        )

        assertEquals(8, result.single().setGroups.single().targetReps)
    }

    @Test
    fun `основные движения помечаются isMain, подсобка нет`() {
        val result = useCase(
            listOf(
                exercise("Присед", "squat"),
                exercise("Подъём на бицепс", "accessory")
            ),
            profile
        )

        val squat = result.first { it.liftType == "squat" }
        val accessory = result.first { it.liftType == "accessory" }
        assertTrue(squat.isMain)
        assertFalse(accessory.isMain)
    }

    @Test
    fun `упражнения с одинаковым именем группируются и сортируются по orderIndex`() {
        val result = useCase(
            listOf(
                exercise("Присед", "squat", order = 2, percent = 0.9),
                exercise("Присед", "squat", order = 1, percent = 0.7)
            ),
            profile
        )

        val group = result.single()
        assertEquals(2, group.setGroups.size)
        // После сортировки по orderIndex первым идёт процент 0.7.
        assertEquals(0.7, group.setGroups[0].percent1rm!!, 0.0001)
        assertEquals(0.9, group.setGroups[1].percent1rm!!, 0.0001)
    }

    @Test
    fun `пустой список даёт пустой результат`() {
        assertTrue(useCase(emptyList(), profile).isEmpty())
    }
}
