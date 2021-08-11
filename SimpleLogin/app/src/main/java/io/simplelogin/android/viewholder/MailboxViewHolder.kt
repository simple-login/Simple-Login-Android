package io.simplelogin.android.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.databinding.RecyclerItemMailboxBinding
import io.simplelogin.android.utils.model.Mailbox

class MailboxViewHolder(private val binding: RecyclerItemMailboxBinding): RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): MailboxViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecyclerItemMailboxBinding.inflate(layoutInflater, parent, false)
            return MailboxViewHolder(binding)
        }
    }

    fun bind(mailbox: Mailbox) {
        binding.emailTextView.text = mailbox.email
        binding.creationDateTextView.text = mailbox.getCreationString()
        binding.aliasCountTextView.text = mailbox.getAliasCountString()
        binding.defaultTextView.visibility = if (mailbox.isDefault) View.VISIBLE else View.GONE
        binding.notVerifiedTextView.visibility = if (mailbox.isVerified) View.GONE else View.VISIBLE
    }
}
