package io.simplelogin.android.module.alias.activity

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import io.simplelogin.android.utils.diffutil.AliasActivityDiffCallback
import io.simplelogin.android.utils.model.AliasActivity
import io.simplelogin.android.viewholder.AliasActivityViewHolder


class AliasActivityListAdapter :
    ListAdapter<AliasActivity, AliasActivityViewHolder>(AliasActivityDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AliasActivityViewHolder =
        AliasActivityViewHolder.from(parent)

    override fun onBindViewHolder(holder: AliasActivityViewHolder, position: Int) =
        holder.bind(getItem(position))
}