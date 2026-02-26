package io.simplelogin.android.usecases.login

import io.simplelogin.android.data.models.preferences.DeviceLockType
import io.simplelogin.android.data.models.preferences.LockTimeOut
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCase
import javax.inject.Inject

interface LogOutUseCase {
    suspend operator fun invoke()
}

class LogOutImpl @Inject constructor(private val updateSessionSettings: UpdateSessionSettingsUseCase) :
    LogOutUseCase {
    override suspend fun invoke() {
        updateSessionSettings {
            it.copy(
                apiKey = null,
                lockType = DeviceLockType.DEFAULT,
                lockTimeOut = LockTimeOut.DEFAULT,
                pinCode = null,
                lastBackgroundTime = 0L,
                numberOfFailedAttempt = 0
            )
        }
    }
}