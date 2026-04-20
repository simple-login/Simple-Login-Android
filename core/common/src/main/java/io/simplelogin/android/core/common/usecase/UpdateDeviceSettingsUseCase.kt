package io.simplelogin.android.core.common.usecase

import androidx.datastore.core.DataStore
import io.simplelogin.android.core.model.preferences.DevicePreferences
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