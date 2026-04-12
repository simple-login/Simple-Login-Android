package io.simplelogin.android.usecases.settings

import androidx.datastore.core.DataStore
import io.simplelogin.android.core.model.preferences.DevicePreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

interface ObserveDeviceSettingsUseCase {
    operator fun invoke(): Flow<DevicePreferences>
}

class ObserveDeviceSettingsUseCaseImpl @Inject constructor(private val datastore: DataStore<DevicePreferences>) :
    ObserveDeviceSettingsUseCase {
    override fun invoke(): Flow<DevicePreferences> = datastore.data
}