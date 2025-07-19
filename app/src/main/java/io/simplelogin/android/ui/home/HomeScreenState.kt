package io.simplelogin.android.ui.home

import io.simplelogin.android.data.models.preferences.DevicePreferences
import io.simplelogin.android.data.models.ui.AliasFilterMode

data class HomeScreenState(
    val deviceSettings: DevicePreferences,
    val aliasFilterMode: AliasFilterMode
) {
    companion object {
        val Default = HomeScreenState(
            deviceSettings = DevicePreferences(),
            aliasFilterMode = AliasFilterMode.ALL
        )
    }
}