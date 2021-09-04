package io.simplelogin.android.module.alias.picker

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import io.simplelogin.android.utils.diffutil.AliasDiffCallback
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.viewholder.AliasLiteViewHolder

class AliasPickerAdapter(private val clickListener: ClickListener) :
    ListAdapter<Alias, AliasLiteViewHolder>(AliasDiffCallback()) {
    interface ClickListener {
        fun onClick(alias: Alias)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AliasLiteViewHolder =
        AliasLiteViewHolder.from(parent)

    override fun onBindViewHolder(holder: AliasLiteViewHolder, position: Int) =
        holder.bind(getItem(position), clickListener)
}
