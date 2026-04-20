package io.simplelogin.feature.createalias

import io.simplelogin.core.model.api.Alias
import io.simplelogin.core.model.api.AliasOptions
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.Mailbox
import io.simplelogin.core.model.preferences.DevicePreferences

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