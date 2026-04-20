package io.simplelogin.android.ui.root

import io.simplelogin.core.model.api.ApiKey

data class AppRootState(
    val isReady: Boolean,
    val apiKey: ApiKey?
) {
    companion object {
        val Default = AppRootState(
            isReady = false,
            apiKey = null
        )
    }
}