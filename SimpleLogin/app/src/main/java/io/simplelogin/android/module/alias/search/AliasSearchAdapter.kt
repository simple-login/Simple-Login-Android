package io.simplelogin.android.module.alias.search

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import io.simplelogin.android.module.alias.AliasListAdapter
import io.simplelogin.android.utils.diffutil.AliasDiffCallback
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.viewholder.AliasViewHolder

class AliasSearchAdapter(private val searchMode: AliasSearchMode,
                         private val clickListener: AliasListAdapter.ClickListener) :
    ListAdapter<Alias, AliasViewHolder>(AliasDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AliasViewHolder =
        AliasViewHolder.from(parent)

    override fun onBindViewHolder(holder: AliasViewHolder, position: Int) =
        holder.bind(getItem(position), searchMode, clickListener)
}
