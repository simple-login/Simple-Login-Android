package io.simplelogin.android.ui.home.createalias

import io.simplelogin.android.models.api.Alias
import io.simplelogin.android.models.api.AliasOptions
import io.simplelogin.android.models.api.ApiError
import io.simplelogin.android.models.api.Mailbox
import io.simplelogin.android.models.preferences.DevicePreferences

data class CreateAliasState(
    val isLoading: Boolean = true,
    val defaultPrefix: String? = null,
    val randomCharacterCount: Int = DevicePreferences.Default.prefixRandomCharacterCount,
    val aliasOptions: AliasOptions? = null,
    val mailboxes: List<Mailbox>? = null,
    val fetchError: ApiError? = null,
    val createError: ApiError? = null,
    val createdAlias: Alias? = null
) {
    companion object {
        val Default = CreateAliasState()
    }
}