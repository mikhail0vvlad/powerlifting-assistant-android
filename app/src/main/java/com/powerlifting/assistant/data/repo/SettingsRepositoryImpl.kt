package com.powerlifting.assistant.data.repo

import com.powerlifting.assistant.data.local.AppPreferences
import com.powerlifting.assistant.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val prefs: AppPreferences
) : SettingsRepository {

    override val darkTheme: Flow<Boolean> = prefs.darkTheme

    override suspend fun setDarkTheme(enabled: Boolean) = prefs.setDarkTheme(enabled)
}
