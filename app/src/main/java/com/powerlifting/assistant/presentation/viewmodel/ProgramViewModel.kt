package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.domain.model.ActiveProgram
import com.powerlifting.assistant.domain.model.ProgramSchedule
import com.powerlifting.assistant.domain.usecase.profile.GetProfileUseCase
import com.powerlifting.assistant.domain.usecase.program.GenerateProgramUseCase
import com.powerlifting.assistant.domain.usecase.program.GetActiveProgramUseCase
import com.powerlifting.assistant.domain.usecase.program.MarkTrainingCompletedUseCase
import com.powerlifting.assistant.domain.usecase.program.RescheduleWorkoutUseCase
import com.powerlifting.assistant.domain.usecase.program.SkipWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ProgramViewModel @Inject constructor(
    private val getActiveProgram: GetActiveProgramUseCase,
    private val generateProgram: GenerateProgramUseCase,
    private val getProfile: GetProfileUseCase,
    private val rescheduleWorkout: RescheduleWorkoutUseCase,
    private val skipWorkout: SkipWorkoutUseCase,
    private val markCompleted: MarkTrainingCompletedUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val active: ActiveProgram? = null,
        val profileMissingMaxes: Boolean = false,
        val mutating: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val profile = getProfile()
                val active = getActiveProgram()
                _state.update {
                    it.copy(
                        loading = false,
                        active = active,
                        profileMissingMaxes = !profile.profile.hasAllMaxes
                    )
                }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }

    /**
     * Generates a program with the user's chosen weekday set + start date.
     * If [weekdays] is empty, falls back to manually-picked [explicitDates].
     * If both are empty, the server uses its Mon/Wed/Fri default.
     */
    fun generate(
        startDate: LocalDate,
        weeks: Int = 4,
        weekdays: Set<DayOfWeek> = emptySet(),
        explicitDates: List<LocalDate> = emptyList()
    ) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val schedule: ProgramSchedule? = when {
                    explicitDates.isNotEmpty() -> ProgramSchedule.Dates(explicitDates)
                    weekdays.isNotEmpty() -> ProgramSchedule.Weekdays(weekdays)
                    else -> null
                }
                generateProgram(startDate = startDate, weeks = weeks, schedule = schedule)
                val active = getActiveProgram()
                _state.update { it.copy(loading = false, active = active) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }

    fun reschedule(workoutId: String, newDate: LocalDate) {
        viewModelScope.launch {
            _state.update { it.copy(mutating = true, error = null) }
            try {
                rescheduleWorkout(workoutId, newDate)
                val active = getActiveProgram()
                _state.update { it.copy(mutating = false, active = active) }
            } catch (t: Throwable) {
                _state.update { it.copy(mutating = false, error = errorMessage(t)) }
            }
        }
    }

    fun skip(workoutId: String) {
        viewModelScope.launch {
            _state.update { it.copy(mutating = true, error = null) }
            try {
                skipWorkout(workoutId)
                val active = getActiveProgram()
                _state.update { it.copy(mutating = false, active = active) }
            } catch (t: Throwable) {
                _state.update { it.copy(mutating = false, error = errorMessage(t)) }
            }
        }
    }

    fun complete(workoutId: String) {
        viewModelScope.launch {
            _state.update { it.copy(mutating = true, error = null) }
            try {
                markCompleted(workoutId)
                val active = getActiveProgram()
                _state.update { it.copy(mutating = false, active = active) }
            } catch (t: Throwable) {
                _state.update { it.copy(mutating = false, error = errorMessage(t)) }
            }
        }
    }
}
