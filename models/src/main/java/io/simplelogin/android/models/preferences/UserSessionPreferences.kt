package io.simplelogin.android.models.preferences

import io.simplelogin.android.models.Constants
import io.simplelogin.android.models.api.ApiKey
import io.simplelogin.android.models.api.UserInfo
import kotlinx.serialization.Serializable

@Serializable
data class UserSessionPreferences(
    val baseUrl: String = Constants.DEFAULT_BASE_URL,
    val apiKey: ApiKey? = null,
    val userInfo: UserInfo? = null,
    val lockType: DeviceLockType = DeviceLockType.DEFAULT,
    val lockTimeOut: LockTimeOut = LockTimeOut.DEFAULT,
    val pinCode: String? = null,
    val lastBackgroundTime: Long = 0L,
    val numberOfFailedAttempt: Int = 0
)

enum class DeviceLockType {
    NONE, BIOMETRIC, PIN;

    companion object {
        val DEFAULT: DeviceLockType = NONE
    }
}

enum class LockTimeOut {
    IMMEDIATE, ONE_MINUTE, TWO_MINUTES, FIVE_MINUTES, TEN_MINUTES, ONE_HOUR;

    companion object {
        val DEFAULT: LockTimeOut = IMMEDIATE
    }

    fun toMillis(): Long = when (this) {
        IMMEDIATE -> 0L
        ONE_MINUTE -> 60_000L
        TWO_MINUTES -> 120_000L
        FIVE_MINUTES -> 300_000L
        TEN_MINUTES -> 600_000L
        ONE_HOUR -> 3_600_000L
    }
}