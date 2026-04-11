package io.simplelogin.android.ui.home

import io.simplelogin.android.data.models.api.Stats
import io.simplelogin.android.data.models.api.UserInfo
import io.simplelogin.android.data.models.ui.AliasFilterMode
import io.simplelogin.android.models.api.Alias
import io.simplelogin.android.models.api.ApiError
import io.simplelogin.android.models.preferences.DevicePreferences

data class HomeScreenState(
    val userInfo: UserInfo?,
    val deviceSettings: DevicePreferences,
    val aliasFilterMode: AliasFilterMode,
    private val stats: Stats?,
    val aliases: List<Alias>,
    val fetchError: ApiError?,
    val isFetching: Boolean,
    val isRefreshing: Boolean
) {
    companion object {
        val Default = HomeScreenState(
            userInfo = null,
            deviceSettings = DevicePreferences(),
            aliasFilterMode = AliasFilterMode.ALL,
            stats = null,
            aliases = listOf(),
            fetchError = null,
            isFetching = false,
            isRefreshing = false
        )
    }

    val displayedStats: Stats?
        get() = if (deviceSettings.showStats && aliasFilterMode == AliasFilterMode.ALL) stats else null
}