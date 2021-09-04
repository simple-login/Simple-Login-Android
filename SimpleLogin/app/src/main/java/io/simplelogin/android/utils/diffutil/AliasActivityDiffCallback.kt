package io.simplelogin.android.utils.diffutil

import androidx.recyclerview.widget.DiffUtil
import io.simplelogin.android.utils.model.AliasActivity

class AliasActivityDiffCallback : DiffUtil.ItemCallback<AliasActivity>() {
    override fun areItemsTheSame(oldItem: AliasActivity, newItem: AliasActivity): Boolean =
        oldItem.timestamp == newItem.timestamp

    override fun areContentsTheSame(oldItem: AliasActivity, newItem: AliasActivity): Boolean =
        oldItem == newItem
}
