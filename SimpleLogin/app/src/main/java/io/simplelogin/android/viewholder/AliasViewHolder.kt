package io.simplelogin.android.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.R
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

    private val context: Context by lazy { binding.root.context }

    fun bind(alias: Alias) {
        binding.emailTextView.text = alias.email

        binding.enabledSwitch.isEnabled = alias.enabled
        if (alias.enabled) {
            binding.rootRelativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorMediumGray))
        } else {
            binding.rootRelativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
        }
    }
}