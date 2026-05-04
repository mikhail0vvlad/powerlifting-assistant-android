package com.powerlifting.assistant.data.repo

import com.powerlifting.assistant.data.api.GenerateProgramRequest
import com.powerlifting.assistant.data.api.PowerliftingApi
import com.powerlifting.assistant.data.api.RescheduleWorkoutRequest
import com.powerlifting.assistant.data.auth.FirebaseTokenProvider
import com.powerlifting.assistant.data.cache.AppCache
import com.powerlifting.assistant.data.mapper.toDomain
import com.powerlifting.assistant.data.mapper.toDto
import com.powerlifting.assistant.domain.model.ActiveProgram
import com.powerlifting.assistant.domain.model.ProgramSchedule
import com.powerlifting.assistant.domain.model.ProgramWorkout
import com.powerlifting.assistant.domain.model.TrainingCalendar
import com.powerlifting.assistant.domain.model.TrainingProgram
import com.powerlifting.assistant.domain.repository.ProgramRepository
import java.time.LocalDate
import javax.inject.Inject

class ProgramRepositoryImpl @Inject constructor(
    private val api: PowerliftingApi,
    private val tokenProvider: FirebaseTokenProvider,
    private val cache: AppCache
) : ProgramRepository {

    override suspend fun getActive(): ActiveProgram {
        cache.syncUser()
        cache.activeProgram.get(Unit)?.let { return it }
        val auth = tokenProvider.bearerToken()
        val fresh = api.getActiveProgram(auth).toDomain()
        cache.activeProgram.put(Unit, fresh)
        return fresh
    }

    override suspend fun generate(
        startDate: LocalDate?,
        weeks: Int?,
        schedule: ProgramSchedule?
    ): TrainingProgram {
        cache.syncUser()
        val auth = tokenProvider.bearerToken(true)
        val request = GenerateProgramRequest(
            startDate = startDate?.toString(),
            weeks = weeks,
            schedule = schedule?.toDto()
        )
        val result = api.generateProgram(auth, request).toDomain()
        cache.afterProgramChange()
        return result
    }

    override suspend fun calendar(from: String, to: String): TrainingCalendar {
        cache.syncUser()
        val key = "$from..$to"
        cache.calendar.get(key)?.let { return it }
        val auth = tokenProvider.bearerToken()
        val fresh = api.getCalendar(auth, from, to).toDomain()
        cache.calendar.put(key, fresh)
        return fresh
    }

    override suspend fun rescheduleWorkout(workoutId: String, newDate: LocalDate): ProgramWorkout {
        cache.syncUser()
        val auth = tokenProvider.bearerToken(true)
        val moved = api.rescheduleWorkout(auth, workoutId, RescheduleWorkoutRequest(newDate.toString())).toDomain()
        cache.afterProgramChange()
        cache.afterWorkoutChange()
        return moved
    }

    override suspend fun skipWorkout(workoutId: String) {
        cache.syncUser()
        val auth = tokenProvider.bearerToken(true)
        api.skipWorkout(auth, workoutId)
        cache.afterProgramChange()
    }
}
