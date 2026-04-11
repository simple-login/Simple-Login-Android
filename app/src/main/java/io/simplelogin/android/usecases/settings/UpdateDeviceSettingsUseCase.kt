package io.simplelogin.android.usecases.settings

import androidx.datastore.core.DataStore
import io.simplelogin.android.models.preferences.DevicePreferences
import javax.inject.Inject

interface UpdateDeviceSettingsUseCase {
    suspend fun invoke(transform: (DevicePreferences) -> DevicePreferences)
}

class UpdateDeviceSettingsUseCaseImpl @Inject constructor(private val dataStore: DataStore<DevicePreferences>) :
    UpdateDeviceSettingsUseCase {
    override suspend fun invoke(transform: (DevicePreferences) -> DevicePreferences) {
        dataStore.updateData(transform)
    }
}