package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.domain.model.WorkoutHistoryItem
import com.powerlifting.assistant.domain.usecase.workout.DeleteWorkoutSessionUseCase
import com.powerlifting.assistant.domain.usecase.workout.GetWorkoutHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import retrofit2.HttpException

@HiltViewModel
class WorkoutHistoryViewModel @Inject constructor(
    private val getWorkoutHistory: GetWorkoutHistoryUseCase,
    private val deleteWorkoutSession: DeleteWorkoutSessionUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val sessions: List<WorkoutHistoryItem> = emptyList(),
        val deletingId: String? = null,
        val deleteError: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val sessions = getWorkoutHistory()
                _state.update { it.copy(loading = false, sessions = sessions) }
            } catch (e: IOException) {
                _state.update { it.copy(loading = false, error = "Нет связи с сервером") }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }

    fun delete(sessionId: String) {
        if (_state.value.deletingId != null) return
        viewModelScope.launch {
            _state.update { it.copy(deletingId = sessionId, deleteError = null) }
            try {
                deleteWorkoutSession(sessionId)
                _state.update { current ->
                    current.copy(
                        deletingId = null,
                        sessions = current.sessions.filterNot { it.sessionId == sessionId }
                    )
                }
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    _state.update { current ->
                        current.copy(
                            deletingId = null,
                            sessions = current.sessions.filterNot { it.sessionId == sessionId },
                            deleteError = "Тренировка уже была удалена. Если ошибка повторяется — перезапустите сервер: новый эндпоинт ещё не развёрнут."
                        )
                    }
                } else {
                    _state.update { it.copy(deletingId = null, deleteError = "Не удалось удалить: ${errorMessage(e)}") }
                }
            } catch (e: IOException) {
                _state.update { it.copy(deletingId = null, deleteError = "Не удалось удалить: нет связи с сервером") }
            } catch (t: Throwable) {
                _state.update { it.copy(deletingId = null, deleteError = "Не удалось удалить: ${errorMessage(t)}") }
            }
        }
    }

    fun dismissDeleteError() {
        _state.update { it.copy(deleteError = null) }
    }
}
