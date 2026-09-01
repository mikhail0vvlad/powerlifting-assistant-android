package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.domain.model.FoodProduct
import com.powerlifting.assistant.domain.repository.FoodRepository
import com.powerlifting.assistant.domain.usecase.nutrition.AddNutritionEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class FoodSearchViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val addNutritionEntry: AddNutritionEntryUseCase,
    private val savedState: SavedStateHandle
) : ViewModel() {

    sealed interface SearchState {
        data object Idle : SearchState
        data object Loading : SearchState
        data class Results(val products: List<FoodProduct>) : SearchState
        data object Empty : SearchState
        data object Error : SearchState
    }

    // Текст запроса хранится в SavedStateHandle — переживает поворот экрана и пересоздание процесса.
    private val _query = MutableStateFlow(savedState.get<String>(KEY_QUERY).orEmpty())
    val query: StateFlow<String> = _query

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState

    val history: StateFlow<List<FoodProduct>> = foodRepository.history()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var searchJob: Job? = null
    private var lastQuery: String = ""

    init {
        // Восстанавливаем результаты после поворота / возврата в экран.
        if (_query.value.isNotBlank()) {
            searchJob = viewModelScope.launch { runSearch(_query.value) }
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
        savedState[KEY_QUERY] = value
        searchJob?.cancel()
        if (value.isBlank()) {
            _searchState.value = SearchState.Idle
            return
        }
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            runSearch(value)
        }
    }

    fun clearQuery() = onQueryChange("")

    /** Повторная отправка последнего запроса (кнопка «Обновить» на плейсхолдере ошибки). */
    fun retry() {
        if (lastQuery.isBlank()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runSearch(lastQuery) }
    }

    private suspend fun runSearch(query: String) {
        lastQuery = query
        _searchState.value = SearchState.Loading
        try {
            val result = foodRepository.search(query)
            _searchState.value =
                if (result.isEmpty()) SearchState.Empty else SearchState.Results(result)
        } catch (t: Throwable) {
            _searchState.value = SearchState.Error
        }
    }

    /** Клик по продукту добавляет его в историю. */
    fun onProductClicked(product: FoodProduct) {
        viewModelScope.launch { foodRepository.addToHistory(product) }
    }

    fun clearHistory() {
        viewModelScope.launch { foodRepository.clearHistory() }
    }

    suspend fun addMeal(product: FoodProduct, grams: Int) {
        val factor = grams / 100.0
        addNutritionEntry(
            "${product.name} ($grams г)",
            (product.calories * factor).roundToInt(),
            (product.proteinG * factor).roundToInt()
        )
    }

    private companion object {
        const val KEY_QUERY = "food_search_query"
        const val DEBOUNCE_MS = 400L
    }
}
