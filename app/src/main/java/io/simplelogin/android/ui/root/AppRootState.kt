package io.simplelogin.android.ui.root

data class AppRootState(
    val isReady: Boolean,
    val apiKey: String?
) {
    companion object {
        val Default = AppRootState(
            isReady = false,
            apiKey = null
        )
    }
}