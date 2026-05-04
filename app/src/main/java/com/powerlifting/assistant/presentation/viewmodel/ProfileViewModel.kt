package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.domain.model.ProfileSummary
import com.powerlifting.assistant.domain.model.ProfileUpdate
import com.powerlifting.assistant.domain.usecase.profile.GetProfileUseCase
import com.powerlifting.assistant.domain.usecase.profile.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfile: GetProfileUseCase,
    private val updateProfile: UpdateProfileUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val profile: ProfileSummary? = null,
        val saved: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, saved = false) }
            try {
                val profile = getProfile()
                _state.update { it.copy(loading = false, profile = profile) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }

    fun save(update: ProfileUpdate) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, saved = false) }
            try {
                val profile = updateProfile(update)
                _state.update { it.copy(loading = false, profile = profile, saved = true) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }
}
