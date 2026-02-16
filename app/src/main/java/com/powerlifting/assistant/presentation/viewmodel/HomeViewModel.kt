package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.domain.model.ProfileSummary
import com.powerlifting.assistant.domain.model.TrainingCalendar
import com.powerlifting.assistant.domain.usecase.profile.GetProfileUseCase
import com.powerlifting.assistant.domain.usecase.program.GetCalendarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProfile: GetProfileUseCase,
    private val getCalendar: GetCalendarUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val profile: ProfileSummary? = null,
        val calendar: TrainingCalendar? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val now = LocalDate.now(ZoneId.systemDefault())
                val from = now.withDayOfMonth(1)
                val to = from.plusMonths(1).minusDays(1)

                val (profile, calendar) = coroutineScope {
                    val profileDeferred = async { getProfile() }
                    val calendarDeferred = async { getCalendar(from.toString(), to.toString()) }
                    profileDeferred.await() to calendarDeferred.await()
                }

                _state.update { it.copy(loading = false, profile = profile, calendar = calendar) }
            } catch (e: IOException) {
                _state.update { it.copy(loading = false, error = "Нет связи с сервером. Проверьте подключение к интернету.") }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }
}
