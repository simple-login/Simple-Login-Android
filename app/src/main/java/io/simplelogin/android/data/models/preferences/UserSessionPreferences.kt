package io.simplelogin.android.data.models.preferences

import io.simplelogin.android.data.util.Constants
import kotlinx.serialization.Serializable

@Serializable
data class UserSessionPreferences(
    val baseUrl: String = Constants.DEFAULT_BASE_URL,
    val apiKey: String? = null
)