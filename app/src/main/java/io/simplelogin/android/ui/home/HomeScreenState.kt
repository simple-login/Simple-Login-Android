package io.simplelogin.android.ui.home

import io.simplelogin.core.model.api.Alias
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.Stats
import io.simplelogin.core.model.api.UserInfo
import io.simplelogin.core.model.preferences.DevicePreferences
import io.simplelogin.core.model.ui.AliasFilterMode

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