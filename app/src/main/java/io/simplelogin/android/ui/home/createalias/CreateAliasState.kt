package io.simplelogin.android.ui.home.createalias

import io.simplelogin.android.data.models.api.AliasOptions
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.Mailbox

data class CreateAliasState(
    val isLoading: Boolean,
    val defaultPrefix: String?,
    val aliasOptions: AliasOptions?,
    val mailboxes: List<Mailbox>?,
    val fetchError: ApiError?
) {
    companion object {
        val Default = CreateAliasState(
            isLoading = true,
            defaultPrefix = null,
            aliasOptions = null,
            mailboxes = null,
            fetchError = null
        )
    }
}