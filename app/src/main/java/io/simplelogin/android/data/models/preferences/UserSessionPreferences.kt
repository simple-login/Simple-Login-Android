package io.simplelogin.android.data.models.preferences

import android.content.Context
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.UserInfo
import io.simplelogin.android.data.util.Constants
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

    fun title(context: Context) = when (this) {
        NONE -> context.getString(R.string.none)
        BIOMETRIC -> context.getString(R.string.biometric)
        PIN -> context.getString(R.string.pin_code)
    }
}

enum class LockTimeOut {
    IMMEDIATE, ONE_MINUTE, TWO_MINUTES, FIVE_MINUTES, TEN_MINUTES, ONE_HOUR;

    companion object {
        val DEFAULT: LockTimeOut = IMMEDIATE
    }

    fun title(context: Context) = when (this) {
        IMMEDIATE -> context.getString(R.string.immediately)
        ONE_MINUTE -> context.getString(R.string.after_one_minute)
        TWO_MINUTES -> context.getString(R.string.after_two_minutes)
        FIVE_MINUTES -> context.getString(R.string.after_five_minutes)
        TEN_MINUTES -> context.getString(R.string.after_ten_minutes)
        ONE_HOUR -> context.getString(R.string.after_one_hour)
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