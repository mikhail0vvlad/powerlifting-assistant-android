package com.powerlifting.assistant.data.cache

import com.google.firebase.auth.FirebaseAuth
import com.powerlifting.assistant.domain.model.Achievement
import com.powerlifting.assistant.domain.model.ActiveProgram
import com.powerlifting.assistant.domain.model.NutritionDay
import com.powerlifting.assistant.domain.model.ProfileSummary
import com.powerlifting.assistant.domain.model.TrainingCalendar
import com.powerlifting.assistant.domain.model.WorkoutHistoryItem
import com.powerlifting.assistant.domain.model.WorkoutSessionDetail
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Singleton
class AppCache @Inject constructor() {

    @Volatile
    private var activeUid: String? = null

    val profile = MemoryCache<Unit, ProfileSummary>(60.seconds)
    val nutritionToday = MemoryCache<String, NutritionDay>(30.seconds)
    val activeProgram = MemoryCache<Unit, ActiveProgram>(5.minutes)
    val calendar = MemoryCache<String, TrainingCalendar>(5.minutes)
    val workoutHistory = MemoryCache<Int, List<WorkoutHistoryItem>>(30.seconds)
    val sessionDetail = MemoryCache<String, WorkoutSessionDetail>(30.seconds)
    val achievements = MemoryCache<Unit, List<Achievement>>(60.seconds)

    @Synchronized
    fun syncUser() {
        val uid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        if (activeUid != uid) {
            clear()
            activeUid = uid
        }
    }

    fun afterProfileChange() {
        profile.invalidateAll()
    }

    fun afterNutritionChange() {
        nutritionToday.invalidateAll()
        profile.invalidateAll()
    }

    fun afterProgramChange() {
        activeProgram.invalidateAll()
        calendar.invalidateAll()
    }

    fun afterWorkoutChange(sessionId: String? = null) {
        workoutHistory.invalidateAll()
        if (sessionId != null) sessionDetail.invalidate(sessionId) else sessionDetail.invalidateAll()
        activeProgram.invalidateAll()
        calendar.invalidateAll()
    }

    fun afterAchievementChange() {
        achievements.invalidateAll()
        profile.invalidateAll()
    }

    fun clear() {
        profile.invalidateAll()
        nutritionToday.invalidateAll()
        activeProgram.invalidateAll()
        calendar.invalidateAll()
        workoutHistory.invalidateAll()
        sessionDetail.invalidateAll()
        achievements.invalidateAll()
    }
}
