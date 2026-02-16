package io.simplelogin.android.ui.home.dialog

import io.simplelogin.android.data.models.api.AliasOptions
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.Mailbox

data class CustomAliasDialogState(
    val isLoading: Boolean,
    val defaultPrefix: String?,
    val aliasOptions: AliasOptions?,
    val mailboxes: List<Mailbox>?,
    val fetchError: ApiError?
) {
    companion object {
        val Default = CustomAliasDialogState(
            isLoading = true,
            defaultPrefix = null,
            aliasOptions = null,
            mailboxes = null,
            fetchError = null
        )
    }
}