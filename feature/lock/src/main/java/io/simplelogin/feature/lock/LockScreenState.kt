package io.simplelogin.feature.lock

import io.simplelogin.core.model.preferences.DeviceLockType

internal sealed class LockScreenState {
    data object Loading : LockScreenState()
    data object Unprotected : LockScreenState()
    data class Protected(val lockType: DeviceLockType, val pinCode: String?) : LockScreenState()

    val isPinProtected: Boolean
        get() = (this as? Protected)?.lockType == DeviceLockType.PIN

    val isBiometricProtected: Boolean
        get() = (this as? Protected)?.lockType == DeviceLockType.BIOMETRIC
}