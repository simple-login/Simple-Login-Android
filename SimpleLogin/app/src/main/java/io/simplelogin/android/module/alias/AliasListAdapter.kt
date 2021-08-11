package io.simplelogin.android.module.alias

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import io.simplelogin.android.module.alias.search.AliasSearchMode
import io.simplelogin.android.utils.diffutil.AliasDiffCallback
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.viewholder.AliasViewHolder

class AliasListAdapter(private val clickListener: ClickListener) :
    ListAdapter<Alias, AliasViewHolder>(AliasDiffCallback()) {
    interface ClickListener {
        fun onClick(alias: Alias)
        fun onSwitch(alias: Alias, position: Int)
        fun onCopy(alias: Alias)
        fun onSendEmail(alias: Alias)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AliasViewHolder =
        AliasViewHolder.from(parent)

    override fun onBindViewHolder(holder: AliasViewHolder, position: Int) =
        holder.bind(getItem(position), AliasSearchMode.DEFAULT, clickListener)
}
