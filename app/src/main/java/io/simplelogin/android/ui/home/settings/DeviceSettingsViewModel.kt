package io.simplelogin.android.ui.home.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.preferences.AliasCellSelection
import io.simplelogin.android.data.models.preferences.AliasDisplayMode
import io.simplelogin.android.data.models.preferences.DevicePreferences
import io.simplelogin.android.data.models.preferences.SwipeAction
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import io.simplelogin.android.usecases.settings.UpdateDeviceSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceSettingsViewModel @Inject constructor(
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

    fun updateSwipeFromLeftToRight(action: SwipeAction) {
        viewModelScope.launch {
            val oldSwipeFromLeftToRight = deviceSettings.value.swipeFromLeftToRightAction
            updateDeviceSettingsUseCase.invoke {
                it.copy(swipeFromLeftToRightAction = action)
            }
            if (action == deviceSettings.value.swipeFromRightToLeftAction) {
                updateDeviceSettingsUseCase.invoke {
                    it.copy(swipeFromRightToLeftAction = oldSwipeFromLeftToRight)
                }
            }
        }
    }

    fun updateSwipeFromRightToLeft(action: SwipeAction) {
        viewModelScope.launch {
            val oldSwipeFromRightToLeft = deviceSettings.value.swipeFromRightToLeftAction
            updateDeviceSettingsUseCase.invoke {
                it.copy(swipeFromRightToLeftAction = action)
            }
            if (action == deviceSettings.value.swipeFromLeftToRightAction) {
                updateDeviceSettingsUseCase.invoke {
                    it.copy(swipeFromLeftToRightAction = oldSwipeFromRightToLeft)
                }
            }
        }
    }

    fun updateAliasDisplayMode(mode: AliasDisplayMode) {
        viewModelScope.launch {
            updateDeviceSettingsUseCase.invoke {
                it.copy(aliasDisplayMode = mode)
            }
        }
    }
}