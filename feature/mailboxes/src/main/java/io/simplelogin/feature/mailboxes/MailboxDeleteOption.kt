package io.simplelogin.feature.mailboxes

import android.content.Context
import io.simplelogin.core.model.api.Mailbox

internal sealed class MailboxDeleteOption(open val mailbox: Mailbox?) {
    data object DeleteAliases : MailboxDeleteOption(null)
    data class TransferAliases(override val mailbox: Mailbox) : MailboxDeleteOption(mailbox)

    fun description(context: Context): String =
        when (this) {
            is DeleteAliases -> context.getString(R.string.delete_aliases)
            is TransferAliases -> context.getString(
                R.string.transfer_aliases_to_mailbox,
                mailbox.email
            )
        }
}
