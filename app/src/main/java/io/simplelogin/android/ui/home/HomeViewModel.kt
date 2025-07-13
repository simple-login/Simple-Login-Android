package io.simplelogin.android.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.android.data.models.preferences.DevicePreferences
import io.simplelogin.android.domain.snackbar.SnackbarConfiguration
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.usecases.CopyToClipboardUseCase
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snackbarManager: SnackbarManager,
    private val copyToClipboardUseCase: CopyToClipboardUseCase,
    observeSessionSettings: ObserveSessionSettingsUseCase,
    observeDeviceSettingsUseCase: ObserveDeviceSettingsUseCase
): ViewModel() {
    val deviceSettings = observeDeviceSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DevicePreferences.Default
        )

    fun copyAliasAddress(email: String) {
        viewModelScope.launch {
            copyToClipboardUseCase.invoke(
                label = context.getString(R.string.alias_address_label),
                content = email
            )
            val message = context.getString(R.string.alias_address_copied, email)
            snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
        }
    }
}