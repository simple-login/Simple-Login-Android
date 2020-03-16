package io.simplelogin.android.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.databinding.RecyclerItemAliasBinding
import io.simplelogin.android.utils.model.Alias

class AliasViewHolder(private val binding: RecyclerItemAliasBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): AliasViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecyclerItemAliasBinding.inflate(layoutInflater, parent, false)
            return AliasViewHolder(binding)
        }
    }

    fun bind(alias: Alias) {
        binding.emailTextView.text = alias.email
    }
}