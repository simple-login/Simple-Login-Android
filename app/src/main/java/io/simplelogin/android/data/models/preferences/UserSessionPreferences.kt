package io.simplelogin.android.data.models.preferences

import io.simplelogin.android.data.util.Constants
import kotlinx.serialization.Serializable

@Serializable
data class UserSessionPreferences(
    val baseUrl: String = Constants.DEFAULT_BASE_URL,
    val apiKey: String? = null,
    val lockEnabled: Boolean = false,
    val lockTimeOut: LockTimeOut = LockTimeOut.DEFAULT,
)

enum class LockTimeOut {
    IMMEDIATE, ONE_MINUTE, TWO_MINUTES, FIVE_MINUTES, TEN_MINUTES, ONE_HOUR;

    companion object {
        val DEFAULT: LockTimeOut = TWO_MINUTES
    }
}