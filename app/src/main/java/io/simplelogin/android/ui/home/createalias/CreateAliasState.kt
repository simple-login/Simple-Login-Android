package io.simplelogin.android.ui.home.createalias

import io.simplelogin.android.data.models.api.AliasOptions
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.Mailbox
import io.simplelogin.android.data.models.preferences.DevicePreferences

data class CreateAliasState(
    val isLoading: Boolean,
    val defaultPrefix: String?,
    val randomCharacterCount: Int,
    val aliasOptions: AliasOptions?,
    val mailboxes: List<Mailbox>?,
    val fetchError: ApiError?
) {
    companion object {
        val Default = CreateAliasState(
            isLoading = true,
            defaultPrefix = null,
            randomCharacterCount = DevicePreferences.Default.prefixRandomCharacterCount,
            aliasOptions = null,
            mailboxes = null,
            fetchError = null
        )
    }
}