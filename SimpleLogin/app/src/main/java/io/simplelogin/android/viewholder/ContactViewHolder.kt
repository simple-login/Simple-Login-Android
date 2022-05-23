package io.simplelogin.android.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.databinding.RecyclerItemContactBinding
import io.simplelogin.android.module.alias.contact.ContactListAdapter
import io.simplelogin.android.utils.model.Contact

class ContactViewHolder(private val binding: RecyclerItemContactBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): ContactViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecyclerItemContactBinding.inflate(layoutInflater, parent, false)
            return ContactViewHolder(binding)
        }
    }

    fun bind(contact: Contact, clickListener: ContactListAdapter.ClickListener) {
        binding.emailTextView.text = contact.email
        binding.creationDateTextView.text = contact.getCreationString()

        val lastEmailSentString = contact.getLastEmailSentString()
        if (lastEmailSentString != null) {
            binding.lastEmailSentTextView.text = lastEmailSentString
            binding.lastEmailSentTextView.visibility = View.VISIBLE
        } else {
            binding.lastEmailSentTextView.text = null
            binding.lastEmailSentTextView.visibility = View.GONE
        }

        binding.informationLinearLayout.alpha = if (contact.blockForward) 0.5f else 1.0f
        binding.blockedEmailTextView.visibility = if (contact.blockForward) View.VISIBLE else View.GONE

        binding.rootCardView.setOnClickListener { clickListener.onClick(contact) }
    }
}
