package io.simplelogin.core.common.usecase

import androidx.datastore.core.DataStore
import io.simplelogin.core.model.preferences.UserSessionPreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

interface ObserveSessionSettingsUseCase {
    operator fun invoke(): Flow<UserSessionPreferences>
}

class ObserveSessionSettingsUseCaseImpl @Inject constructor(private val dataStore: DataStore<UserSessionPreferences>) :
    ObserveSessionSettingsUseCase {
    override fun invoke(): Flow<UserSessionPreferences> = dataStore.data
}