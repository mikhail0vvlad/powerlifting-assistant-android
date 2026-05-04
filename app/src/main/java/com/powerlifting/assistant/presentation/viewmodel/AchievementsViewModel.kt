package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.domain.model.Achievement
import com.powerlifting.assistant.domain.usecase.achievements.CreateAchievementUseCase
import com.powerlifting.assistant.domain.usecase.achievements.DeleteAchievementUseCase
import com.powerlifting.assistant.domain.usecase.achievements.GetAchievementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val getAchievements: GetAchievementsUseCase,
    private val createAchievement: CreateAchievementUseCase,
    private val deleteAchievement: DeleteAchievementUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val items: List<Achievement> = emptyList()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val list = getAchievements()
                _state.update { it.copy(loading = false, items = list) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }

    fun add(note: String) {
        viewModelScope.launch {
            try {
                createAchievement(note)
                load()
            } catch (t: Throwable) {
                _state.update { it.copy(error = errorMessage(t)) }
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            try {
                deleteAchievement(id)
                load()
            } catch (t: Throwable) {
                _state.update { it.copy(error = errorMessage(t)) }
            }
        }
    }
}
