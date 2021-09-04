package io.simplelogin.android.module.alias.contact

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import io.simplelogin.android.utils.diffutil.ContactDiffCallback
import io.simplelogin.android.utils.model.Contact
import io.simplelogin.android.viewholder.ContactViewHolder

class ContactListAdapter(private val clickListener: ClickListener) :
    ListAdapter<Contact, ContactViewHolder>(ContactDiffCallback()) {
    interface ClickListener {
        fun onClick(contact: Contact)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder =
        ContactViewHolder.from(parent)

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) =
        holder.bind(getItem(position), clickListener)
}
