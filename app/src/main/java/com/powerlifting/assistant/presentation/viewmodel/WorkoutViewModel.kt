package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.domain.model.ExerciseGroup
import com.powerlifting.assistant.domain.model.SetGroupInfo
import com.powerlifting.assistant.domain.model.WorkoutSet
import com.powerlifting.assistant.domain.usecase.profile.GetProfileUseCase
import com.powerlifting.assistant.domain.usecase.workout.AddWorkoutSetsUseCase
import com.powerlifting.assistant.domain.usecase.workout.FinishWorkoutSessionUseCase
import com.powerlifting.assistant.domain.usecase.workout.GetWorkoutSessionDetailUseCase
import com.powerlifting.assistant.domain.usecase.workout.GroupExercisesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

enum class WorkoutPhase {
    LOADING, WARMUP, EXERCISE, REST, ACCESSORIES, FINISH_RATING, FINISHED
}

data class WorkoutUiState(
    val phase: WorkoutPhase = WorkoutPhase.LOADING,
    val error: String? = null,
    val recommendation: String? = null,

    val mainExercises: List<ExerciseGroup> = emptyList(),
    val accessoryExercises: List<ExerciseGroup> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val currentSetGroupIndex: Int = 0,

    val exerciseTimerSec: Int = 0,
    val restTimerSec: Int = 0,
    val totalTimerSec: Int = 0,
    val isExerciseTimerRunning: Boolean = false,

    val completedSetsList: List<WorkoutSet> = emptyList(),

    val wellbeingRating: Int = 3,
    val isFinishing: Boolean = false
) {
    val currentExercise: ExerciseGroup?
        get() = mainExercises.getOrNull(currentExerciseIndex)

    val currentSetGroup: SetGroupInfo?
        get() = currentExercise?.setGroups?.getOrNull(currentSetGroupIndex)

    val totalMainSets: Int
        get() = mainExercises.sumOf { ex -> ex.setGroups.sumOf { it.totalSets } }

    val completedMainSets: Int
        get() = mainExercises.sumOf { ex -> ex.setGroups.sumOf { it.completedSets } }

    val allMainExercisesDone: Boolean
        get() = currentExerciseIndex >= mainExercises.size
}

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val getSessionDetail: GetWorkoutSessionDetailUseCase,
    private val getProfile: GetProfileUseCase,
    private val groupExercises: GroupExercisesUseCase,
    private val addWorkoutSets: AddWorkoutSetsUseCase,
    private val finishWorkoutSession: FinishWorkoutSessionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutUiState())
    val state: StateFlow<WorkoutUiState> = _state

    private var timerJob: Job? = null
    private var totalTimerJob: Job? = null
    private var sessionId: String = ""

    fun loadWorkout(sessionId: String) {
        this.sessionId = sessionId
        viewModelScope.launch {
            _state.update { it.copy(phase = WorkoutPhase.LOADING, error = null) }
            try {
                val profile = getProfile()
                val detail = getSessionDetail(sessionId)

                val grouped = groupExercises(detail.exercises, profile.profile)
                val main = grouped.filter { it.isMain }
                val accessory = grouped.filter { !it.isMain }

                _state.update {
                    it.copy(
                        phase = WorkoutPhase.WARMUP,
                        recommendation = detail.recommendation,
                        mainExercises = main,
                        accessoryExercises = accessory
                    )
                }

                startTotalTimer()
            } catch (e: IOException) {
                _state.update { it.copy(phase = WorkoutPhase.LOADING, error = "Нет связи с сервером") }
            } catch (t: Throwable) {
                _state.update { it.copy(phase = WorkoutPhase.LOADING, error = t.message ?: "Ошибка загрузки") }
            }
        }
    }

    fun warmupDone() {
        if (_state.value.mainExercises.isEmpty()) {
            _state.update { it.copy(phase = WorkoutPhase.ACCESSORIES) }
        } else {
            _state.update { it.copy(phase = WorkoutPhase.EXERCISE, currentExerciseIndex = 0, currentSetGroupIndex = 0) }
        }
    }

    fun startSet() {
        _state.update { it.copy(isExerciseTimerRunning = true, exerciseTimerSec = 0) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(exerciseTimerSec = it.exerciseTimerSec + 1) }
            }
        }
    }

    fun completeSet() {
        timerJob?.cancel()

        val s = _state.value
        val exercise = s.currentExercise ?: return
        val setGroup = s.currentSetGroup ?: return

        val setNumber = s.completedSetsList.size + 1
        val weight = setGroup.weightKg ?: 0.0
        val loggedSet = WorkoutSet(
            exerciseName = exercise.name,
            setNumber = setNumber,
            weightKg = weight,
            reps = setGroup.targetReps,
            rpe = null
        )

        val updatedSets = s.completedSetsList + loggedSet

        val updatedExercises = s.mainExercises.toMutableList()
        val ex = updatedExercises[s.currentExerciseIndex]
        val updatedSetGroups = ex.setGroups.toMutableList()
        updatedSetGroups[s.currentSetGroupIndex] = setGroup.copy(completedSets = setGroup.completedSets + 1)
        updatedExercises[s.currentExerciseIndex] = ex.copy(setGroups = updatedSetGroups)

        _state.update {
            it.copy(
                mainExercises = updatedExercises,
                completedSetsList = updatedSets,
                isExerciseTimerRunning = false,
                exerciseTimerSec = 0,
                restTimerSec = 0,
                phase = WorkoutPhase.REST
            )
        }

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(restTimerSec = it.restTimerSec + 1) }
            }
        }
    }

    fun skipRest() {
        timerJob?.cancel()
        advanceToNextSet()
    }

    private fun advanceToNextSet() {
        val s = _state.value
        val exercise = s.currentExercise ?: return
        val currentSg = exercise.setGroups.getOrNull(s.currentSetGroupIndex)

        if (currentSg != null && !currentSg.allCompleted) {
            _state.update { it.copy(phase = WorkoutPhase.EXERCISE) }
            return
        }

        val nextSgIndex = s.currentSetGroupIndex + 1
        if (nextSgIndex < exercise.setGroups.size) {
            _state.update { it.copy(currentSetGroupIndex = nextSgIndex, phase = WorkoutPhase.EXERCISE) }
            return
        }

        val nextExIndex = s.currentExerciseIndex + 1
        if (nextExIndex < s.mainExercises.size) {
            _state.update { it.copy(currentExerciseIndex = nextExIndex, currentSetGroupIndex = 0, phase = WorkoutPhase.EXERCISE) }
            return
        }

        _state.update { it.copy(phase = WorkoutPhase.ACCESSORIES) }
    }

    fun proceedToFinish() {
        _state.update { it.copy(phase = WorkoutPhase.FINISH_RATING) }
    }

    fun setWellbeingRating(rating: Int) {
        _state.update { it.copy(wellbeingRating = rating) }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            _state.update { it.copy(isFinishing = true, error = null) }
            try {
                val sets = _state.value.completedSetsList
                if (sets.isNotEmpty()) {
                    addWorkoutSets(sessionId, sets)
                }

                finishWorkoutSession(
                    sessionId,
                    _state.value.totalTimerSec,
                    _state.value.wellbeingRating
                )

                totalTimerJob?.cancel()
                _state.update { it.copy(isFinishing = false, phase = WorkoutPhase.FINISHED) }
            } catch (e: IOException) {
                _state.update { it.copy(isFinishing = false, error = "Нет связи с сервером") }
            } catch (t: Throwable) {
                _state.update { it.copy(isFinishing = false, error = t.message ?: "Ошибка") }
            }
        }
    }

    private fun startTotalTimer() {
        totalTimerJob?.cancel()
        totalTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(totalTimerSec = it.totalTimerSec + 1) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        totalTimerJob?.cancel()
    }
}
