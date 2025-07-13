package io.simplelogin.android.ui.home.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.preferences.AliasCellSelection
import io.simplelogin.android.data.models.preferences.DevicePreferences
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import io.simplelogin.android.usecases.settings.UpdateDeviceSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceSettingsDialogViewModel @Inject constructor(
    observeDeviceSettingsUseCase: ObserveDeviceSettingsUseCase,
    private val updateDeviceSettingsUseCase: UpdateDeviceSettingsUseCase
) : ViewModel() {
    val deviceSettings = observeDeviceSettingsUseCase.invoke()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DevicePreferences.Default
        )

    fun updateAliasCellSelection(selection: AliasCellSelection) {
        viewModelScope.launch {
            updateDeviceSettingsUseCase.invoke {
                it.copy(aliasCellSelection = selection)
            }
        }
    }
}