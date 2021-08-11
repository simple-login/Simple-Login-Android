package io.simplelogin.android.module.mailbox

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import io.simplelogin.android.utils.diffutil.MailboxDiffCallback
import io.simplelogin.android.utils.model.Mailbox
import io.simplelogin.android.viewholder.MailboxViewHolder

class MailboxListAdapter : ListAdapter<Mailbox, MailboxViewHolder>(MailboxDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MailboxViewHolder =
        MailboxViewHolder.from(parent)

    override fun onBindViewHolder(holder: MailboxViewHolder, position: Int) =
        holder.bind(getItem(position))
}
