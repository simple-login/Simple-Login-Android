package io.simplelogin.android.ui.home.mailboxes

import io.simplelogin.android.core.model.api.ApiError
import io.simplelogin.android.core.model.api.Mailbox

data class MailboxesState(
    val mailboxes: List<Mailbox>?,
    val isFetching: Boolean,
    val fetchError: ApiError?,
    val isUpdating: Boolean,
    val updateError: ApiError?,
    val addedMailbox: Mailbox?,
    val deletedMailbox: Mailbox?,
    val newDefaultMailbox: Mailbox?
) {
    companion object {
        val Default = MailboxesState(
            mailboxes = null,
            isFetching = true,
            fetchError = null,
            isUpdating = false,
            updateError = null,
            addedMailbox = null,
            deletedMailbox = null,
            newDefaultMailbox = null
        )
    }
}