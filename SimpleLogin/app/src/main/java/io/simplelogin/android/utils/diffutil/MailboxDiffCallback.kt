package io.simplelogin.android.utils.diffutil

import androidx.recyclerview.widget.DiffUtil
import io.simplelogin.android.utils.model.Mailbox

class MailboxDiffCallback : DiffUtil.ItemCallback<Mailbox>() {
    override fun areItemsTheSame(oldItem: Mailbox, newItem: Mailbox): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Mailbox, newItem: Mailbox): Boolean =
        oldItem == newItem
}
