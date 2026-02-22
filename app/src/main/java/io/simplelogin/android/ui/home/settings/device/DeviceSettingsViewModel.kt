package io.simplelogin.android.ui.home.settings.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.preferences.AliasCellSelection
import io.simplelogin.android.data.models.preferences.AliasDisplayInfo
import io.simplelogin.android.data.models.preferences.AliasOptionsDisplay
import io.simplelogin.android.data.models.preferences.Theme
import io.simplelogin.android.data.models.preferences.DefaultPrefix
import io.simplelogin.android.data.models.preferences.DevicePreferences
import io.simplelogin.android.data.models.preferences.SwipeAction
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import io.simplelogin.android.usecases.settings.UpdateDeviceSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DeviceSettingsState {
    data object Loading : DeviceSettingsState()
    data class Loaded(val settings: DevicePreferences) : DeviceSettingsState()
}

@HiltViewModel
class DeviceSettingsViewModel @Inject constructor(
    observeDeviceSettings: ObserveDeviceSettingsUseCase,
    private val updateDeviceSettings: UpdateDeviceSettingsUseCase
) : ViewModel() {
    val stateFlow = observeDeviceSettings().map {
        DeviceSettingsState.Loaded(it)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DeviceSettingsState.Loading
        )

    private val currentSettings: DevicePreferences?
        get() = (stateFlow.value as? DeviceSettingsState.Loaded)?.settings

    fun updateAliasCellSelection(selection: AliasCellSelection) {
        updateSettings { it.copy(aliasCellSelection = selection) }
    }

    fun updateAliasOptionsDisplay(display: AliasOptionsDisplay) {
        updateSettings { it.copy(aliasOptionsDisplay = display) }
    }

    fun updateSwipeFromLeftToRight(action: SwipeAction) {
        withCurrentSettings { currentSettings ->
            val oldSwipeFromLeftToRight = currentSettings.swipeFromLeftToRightAction
            updateDeviceSettings.invoke {
                it.copy(swipeFromLeftToRightAction = action)
            }
            if (action == currentSettings.swipeFromRightToLeftAction) {
                updateDeviceSettings.invoke {
                    it.copy(swipeFromRightToLeftAction = oldSwipeFromLeftToRight)
                }
            }
        }
    }

    fun updateSwipeFromRightToLeft(action: SwipeAction) {
        withCurrentSettings { currentSettings ->
            val oldSwipeFromRightToLeft = currentSettings.swipeFromRightToLeftAction
            updateDeviceSettings.invoke {
                it.copy(swipeFromRightToLeftAction = action)
            }
            if (action == currentSettings.swipeFromLeftToRightAction) {
                updateDeviceSettings.invoke {
                    it.copy(swipeFromLeftToRightAction = oldSwipeFromRightToLeft)
                }
            }
        }
    }

    fun updateAliasDisplayInfos(infos: List<AliasDisplayInfo>) {
        updateSettings { it.copy(aliasDisplayInfos = infos) }
    }

    fun updateDefaultPrefix(defaultPrefix: DefaultPrefix) {
        viewModelScope.launch {
            updateDeviceSettings.invoke {
                it.copy(defaultPrefix = defaultPrefix)
            }
        }
        updateSettings { it.copy(defaultPrefix = defaultPrefix) }
    }

    fun updateRandomCharacterCount(count: Int) {
        updateSettings { it.copy(prefixRandomCharacterCount = count) }
    }

    fun updateCopyAfterCreating(copyAfterCreating: Boolean) {
        updateSettings { it.copy(copyAfterCreating = copyAfterCreating) }
    }

    fun updateAskForRandomAliasNote(ask: Boolean) {
        updateSettings { it.copy(askForRandomAliasNote = ask) }
    }

    fun updateShowStats(showStats: Boolean) {
        updateSettings { it.copy(showStats = showStats) }
    }

    fun updateTheme(theme: Theme) {
        updateSettings { it.copy(theme = theme) }
    }

    private fun updateSettings(update: (DevicePreferences) -> DevicePreferences) {
        viewModelScope.launch {
            updateDeviceSettings.invoke {
                update(it)
            }
        }
    }

    private fun withCurrentSettings(block: suspend (DevicePreferences) -> Unit) {
        viewModelScope.launch {
            when (val value = stateFlow.value) {
                is DeviceSettingsState.Loaded -> block(value.settings)
                DeviceSettingsState.Loading -> {}
            }
        }
    }
}