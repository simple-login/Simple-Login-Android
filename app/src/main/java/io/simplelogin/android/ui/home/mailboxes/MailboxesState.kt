package io.simplelogin.android.ui.home.mailboxes

import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.Mailbox

data class MailboxesState(
    val mailboxes: List<Mailbox>?,
    val isFetching: Boolean,
    val fetchError: ApiError?,
    val isUpdating: Boolean,
    val updateError: ApiError?
) {
    companion object {
        val Default = MailboxesState(
            mailboxes = null,
            isFetching = true,
            fetchError = null,
            isUpdating = false,
            updateError = null
        )
    }
}