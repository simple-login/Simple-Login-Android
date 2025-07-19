package io.simplelogin.android.ui.home

import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.preferences.DevicePreferences
import io.simplelogin.android.data.models.ui.AliasFilterMode

data class HomeScreenState(
    val deviceSettings: DevicePreferences,
    val aliasFilterMode: AliasFilterMode,
    val aliases: List<Alias>,
    val isFetching: Boolean
) {
    companion object {
        val Default = HomeScreenState(
            deviceSettings = DevicePreferences(),
            aliasFilterMode = AliasFilterMode.ALL,
            aliases = listOf(),
            isFetching = false
        )
    }
}