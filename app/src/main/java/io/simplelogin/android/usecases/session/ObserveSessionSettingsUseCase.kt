package io.simplelogin.android.usecases.session

import androidx.datastore.core.DataStore
import io.simplelogin.android.data.models.preferences.UserSessionPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ObserveSessionSettingsUseCase {
    operator fun invoke(): Flow<UserSessionPreferences>
}

class ObserveSessionSettingsUseCaseImpl @Inject constructor(private val dataStore: DataStore<UserSessionPreferences>) :
    ObserveSessionSettingsUseCase {
    override fun invoke(): Flow<UserSessionPreferences> = dataStore.data
}