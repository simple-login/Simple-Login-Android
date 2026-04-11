package io.simplelogin.android.usecases.settings

import androidx.datastore.core.DataStore
import io.simplelogin.android.models.preferences.DevicePreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ObserveDeviceSettingsUseCase {
    operator fun invoke(): Flow<DevicePreferences>
}

class ObserveDeviceSettingsUseCaseImpl @Inject constructor(private val datastore: DataStore<DevicePreferences>) :
    ObserveDeviceSettingsUseCase {
    override fun invoke(): Flow<DevicePreferences> = datastore.data
}