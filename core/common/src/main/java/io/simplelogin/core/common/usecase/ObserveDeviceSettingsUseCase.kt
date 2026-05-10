package io.simplelogin.core.common.usecase

import androidx.datastore.core.DataStore
import io.simplelogin.core.model.preferences.DevicePreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ObserveDeviceSettingsUseCase {
    operator fun invoke(): Flow<DevicePreferences>
}

class ObserveDeviceSettingsUseCaseImpl @Inject constructor(private val datastore: DataStore<DevicePreferences>) :
    ObserveDeviceSettingsUseCase {
    override fun invoke(): Flow<DevicePreferences> = datastore.data
}
