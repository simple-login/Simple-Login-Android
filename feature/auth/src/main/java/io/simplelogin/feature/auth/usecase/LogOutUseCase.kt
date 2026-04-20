package io.simplelogin.feature.auth.usecase

import io.simplelogin.core.common.usecase.UpdateSessionSettingsUseCase
import io.simplelogin.core.model.preferences.DeviceLockType
import io.simplelogin.core.model.preferences.LockTimeOut
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
                userInfo = null,
                lockType = DeviceLockType.DEFAULT,
                lockTimeOut = LockTimeOut.DEFAULT,
                pinCode = null,
                lastBackgroundTime = 0L,
                numberOfFailedAttempt = 0
            )
        }
    }
}