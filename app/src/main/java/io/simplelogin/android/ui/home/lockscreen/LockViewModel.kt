package io.simplelogin.android.ui.home.lockscreen

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.preferences.DeviceLockType
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(
    private val observeSessionSettings: ObserveSessionSettingsUseCase,
    private val updateSessionSettings: UpdateSessionSettingsUseCase
) : ViewModel() {
    private val _stateFlow = MutableStateFlow<LockScreenState>(LockScreenState.Loading)
    val stateFlow: StateFlow<LockScreenState> = _stateFlow

    suspend fun coverAndRecordLastBackgroundTime() {
        if (_stateFlow.value is LockScreenState.Unprotected) {
            _stateFlow.value = LockScreenState.Loading
            updateSessionSettings {
                it.copy(lastBackgroundTime = System.currentTimeMillis())
            }
        }
    }

    suspend fun updateLockState() {
        val session = observeSessionSettings().first()
        _stateFlow.value = when (session.lockType) {
            DeviceLockType.NONE -> LockScreenState.Unprotected
            DeviceLockType.PIN, DeviceLockType.BIOMETRIC -> {
                val elapsedMillis = System.currentTimeMillis() - session.lastBackgroundTime
                val isLocked = elapsedMillis > session.lockTimeOut.toMillis()
                if (isLocked) {
                    LockScreenState.Protected(
                        lockType = session.lockType,
                        pinCode = session.pinCode
                    )
                } else {
                    LockScreenState.Unprotected
                }
            }
        }
    }

    fun unlock() {
        _stateFlow.value = LockScreenState.Unprotected
    }

    fun logOut() {}
}