package io.simplelogin.android.ui.home.settings.account

import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.UsableDomain
import io.simplelogin.android.data.models.api.UserInfo
import io.simplelogin.android.data.models.api.UserSettings

data class AccountSettings(
    val userInfo: UserInfo,
    val userSettings: UserSettings,
    val usableDomains: List<UsableDomain>
)

data class AccountSettingsState(
    val settings: AccountSettings?,
    val isLoading: Boolean,
    val fetchError: ApiError?,
    val updateError: ApiError?
) {
    companion object {
        val Default = AccountSettingsState(
            settings = null,
            isLoading = true,
            fetchError = null,
            updateError = null
        )
    }
}