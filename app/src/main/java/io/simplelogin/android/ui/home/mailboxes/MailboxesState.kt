package io.simplelogin.android.ui.home.mailboxes

import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.Mailbox

data class MailboxesState(
    val mailboxes: List<Mailbox>?,
    val isLoading: Boolean,
    val fetchError: ApiError?,
    val updateError: ApiError?
) {
    companion object {
        val Default = MailboxesState(
            mailboxes = null,
            isLoading = true,
            fetchError = null,
            updateError = null
        )
    }
}