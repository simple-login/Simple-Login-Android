package io.simplelogin.core.common.usecase

import androidx.datastore.core.DataStore
import io.simplelogin.core.model.preferences.UserSessionPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ObserveSessionSettingsUseCase {
    operator fun invoke(): Flow<UserSessionPreferences>
}

class ObserveSessionSettingsUseCaseImpl @Inject constructor(private val dataStore: DataStore<UserSessionPreferences>) :
    ObserveSessionSettingsUseCase {
    override fun invoke(): Flow<UserSessionPreferences> = dataStore.data
}
