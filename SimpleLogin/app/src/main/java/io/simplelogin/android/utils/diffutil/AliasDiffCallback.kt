package io.simplelogin.android.utils.diffutil

import androidx.recyclerview.widget.DiffUtil
import io.simplelogin.android.utils.model.Alias

class AliasDiffCallback : DiffUtil.ItemCallback<Alias>() {
    override fun areItemsTheSame(oldItem: Alias, newItem: Alias): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Alias, newItem: Alias): Boolean =
        oldItem == newItem
}
