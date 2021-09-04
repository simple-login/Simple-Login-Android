package io.simplelogin.android.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.databinding.RecyclerItemAliasLiteBinding
import io.simplelogin.android.module.alias.picker.AliasPickerAdapter
import io.simplelogin.android.utils.model.Alias

class AliasLiteViewHolder(val binding: RecyclerItemAliasLiteBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): AliasLiteViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecyclerItemAliasLiteBinding.inflate(layoutInflater, parent, false)
            return AliasLiteViewHolder(binding)
        }
    }

    fun bind(alias: Alias, clickListener: AliasPickerAdapter.ClickListener) {
        binding.emailTextView.text = alias.email
        binding.noteTextView.text = alias.note
        binding.noteTextView.visibility = if (alias.note.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.rootCardView.alpha = if (alias.enabled) 1f else 0.8f
        binding.root.setOnClickListener { clickListener.onClick(alias) }
    }
}
