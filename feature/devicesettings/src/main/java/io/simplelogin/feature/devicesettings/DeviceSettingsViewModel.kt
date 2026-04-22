package io.simplelogin.feature.devicesettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.core.common.usecase.ObserveDeviceSettingsUseCase
import io.simplelogin.core.common.usecase.ObserveSessionSettingsUseCase
import io.simplelogin.core.common.usecase.UpdateDeviceSettingsUseCase
import io.simplelogin.core.common.usecase.UpdateSessionSettingsUseCase
import io.simplelogin.core.model.preferences.AliasCellSelection
import io.simplelogin.core.model.preferences.AliasDisplayInfo
import io.simplelogin.core.model.preferences.AliasOptionsDisplay
import io.simplelogin.core.model.preferences.ContactCellSelection
import io.simplelogin.core.model.preferences.DefaultPrefix
import io.simplelogin.core.model.preferences.DeviceLockType
import io.simplelogin.core.model.preferences.DevicePreferences
import io.simplelogin.core.model.preferences.LockTimeOut
import io.simplelogin.core.model.preferences.SwipeAction
import io.simplelogin.core.model.preferences.Theme
import io.simplelogin.core.model.preferences.UserSessionPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DeviceSettingsState {
    data object Loading : DeviceSettingsState()
    data class Loaded(
        val settings: DevicePreferences,
        val session: UserSessionPreferences
    ) : DeviceSettingsState()
}

@HiltViewModel
class DeviceSettingsViewModel @Inject constructor(
    observeDeviceSettings: ObserveDeviceSettingsUseCase,
    observeSessionSettings: ObserveSessionSettingsUseCase,
    private val updateDeviceSettings: UpdateDeviceSettingsUseCase,
    private val updateSessionSettings: UpdateSessionSettingsUseCase
) : ViewModel() {
    val stateFlow = combine(
        observeDeviceSettings(),
        observeSessionSettings()
    ) { settings, session ->
        DeviceSettingsState.Loaded(settings = settings, session = session)
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

    fun updateDynamicColor(dynamic: Boolean) {
        updateSettings { it.copy(dynamicColor = dynamic) }
    }

    fun updateContactCellSelection(selection: ContactCellSelection) {
        updateSettings { it.copy(contactCellSelection = selection) }
    }

    fun removeAutoLock() {
        viewModelScope.launch {
            updateSessionSettings { it.copy(lockType = DeviceLockType.NONE) }
        }
    }

    fun enableBiometricLock() {
        viewModelScope.launch {
            updateSessionSettings { it.copy(lockType = DeviceLockType.BIOMETRIC) }
        }
    }

    fun setPinCode(pin: String) {
        viewModelScope.launch {
            updateSessionSettings { it.copy(lockType = DeviceLockType.PIN, pinCode = pin) }
        }
    }

    fun updateLockTimeout(timeOut: LockTimeOut) {
        viewModelScope.launch {
            updateSessionSettings { it.copy(lockTimeOut = timeOut) }
        }
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