package io.simplelogin.android.ui.home

import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.Stats
import io.simplelogin.android.data.models.preferences.DevicePreferences
import io.simplelogin.android.data.models.ui.AliasFilterMode

data class HomeScreenState(
    val deviceSettings: DevicePreferences,
    val aliasFilterMode: AliasFilterMode,
    val stats: Stats?,
    val aliases: List<Alias>,
    val fetchError: ApiError?,
    val isFetching: Boolean,
    val isRefreshing: Boolean
) {
    companion object {
        val Default = HomeScreenState(
            deviceSettings = DevicePreferences(),
            aliasFilterMode = AliasFilterMode.ALL,
            stats = null,
            aliases = listOf(),
            fetchError = null,
            isFetching = false,
            isRefreshing = false
        )
    }
}