package com.powerlifting.assistant.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.powerlifting.assistant.domain.model.FoodProduct
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

/**
 * Локальное хранилище настроек и истории поиска (DataStore Preferences).
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val SEARCH_HISTORY = stringPreferencesKey("search_history")
    }

    // --- Тёмная тема ---

    val darkTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DARK_THEME] ?: true
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    // --- История поиска ---

    val searchHistory: Flow<List<FoodProduct>> = context.dataStore.data.map { prefs ->
        prefs[Keys.SEARCH_HISTORY]?.let { decode(it) } ?: emptyList()
    }

    suspend fun addToHistory(product: FoodProduct) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.SEARCH_HISTORY]?.let { decode(it) } ?: emptyList()
            // Новые элементы — вверху, без дубликатов, не больше 10.
            val updated = (listOf(product) + current.filterNot { it.id == product.id })
                .take(MAX_HISTORY)
            prefs[Keys.SEARCH_HISTORY] = json.encodeToString(updated)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { it.remove(Keys.SEARCH_HISTORY) }
    }

    private fun decode(raw: String): List<FoodProduct> =
        runCatching { json.decodeFromString<List<FoodProduct>>(raw) }.getOrDefault(emptyList())

    private companion object {
        const val MAX_HISTORY = 10
    }
}
