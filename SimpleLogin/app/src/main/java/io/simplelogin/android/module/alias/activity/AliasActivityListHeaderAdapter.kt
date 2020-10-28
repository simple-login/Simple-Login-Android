package io.simplelogin.android.module.alias.activity

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.viewholder.AliasActivityHeaderViewHolder

class AliasActivityListHeaderAdapter(
    private val viewModel: AliasActivityListViewModel,
    private val clickListener: ClickListener
) : RecyclerView.Adapter<AliasActivityHeaderViewHolder>() {
    interface ClickListener {
        fun editMailboxesButtonClicked()
        fun editNameButtonClicked()
        fun editNoteButtonClicked()
    }

    override fun getItemCount() = 1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AliasActivityHeaderViewHolder =
        AliasActivityHeaderViewHolder.from(parent)

    override fun onBindViewHolder(holder: AliasActivityHeaderViewHolder, position: Int) =
        holder.bind(viewModel.alias, clickListener)
}
