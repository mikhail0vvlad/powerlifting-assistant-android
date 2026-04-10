package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.domain.model.NutritionDay
import com.powerlifting.assistant.domain.model.NutritionEntry
import com.powerlifting.assistant.domain.usecase.nutrition.AddNutritionEntryUseCase
import com.powerlifting.assistant.domain.usecase.nutrition.DeleteNutritionEntryUseCase
import com.powerlifting.assistant.domain.usecase.nutrition.GetNutritionTodayUseCase
import com.powerlifting.assistant.domain.usecase.nutrition.UpdateNutritionGoalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaloriesViewModel @Inject constructor(
    private val getNutritionToday: GetNutritionTodayUseCase,
    private val updateNutritionGoals: UpdateNutritionGoalsUseCase,
    private val addNutritionEntry: AddNutritionEntryUseCase,
    private val deleteNutritionEntry: DeleteNutritionEntryUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val data: NutritionDay? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val data = getNutritionToday()
                _state.update { it.copy(loading = false, data = data) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }

    fun updateGoals(caloriesGoal: Int, proteinGoalG: Int) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                updateNutritionGoals(caloriesGoal, proteinGoalG)
                val data = getNutritionToday()
                _state.update { it.copy(loading = false, data = data) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }

    fun addEntry(title: String, calories: Int, proteinG: Int) {
        viewModelScope.launch {
            try {
                addNutritionEntry(title, calories, proteinG)
                val data = getNutritionToday()
                _state.update { it.copy(data = data) }
            } catch (t: Throwable) {
                _state.update { it.copy(error = errorMessage(t)) }
            }
        }
    }

    fun deleteEntry(entry: NutritionEntry) {
        viewModelScope.launch {
            try {
                deleteNutritionEntry(entry.id)
                val data = getNutritionToday()
                _state.update { it.copy(data = data) }
            } catch (t: Throwable) {
                _state.update { it.copy(error = errorMessage(t)) }
            }
        }
    }
}
