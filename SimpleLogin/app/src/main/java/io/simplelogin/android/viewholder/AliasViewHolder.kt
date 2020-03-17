package io.simplelogin.android.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.R
import io.simplelogin.android.databinding.RecyclerItemAliasBinding
import io.simplelogin.android.utils.model.Alias

class AliasViewHolder(val binding: RecyclerItemAliasBinding) : RecyclerView.ViewHolder(binding.root) {
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
        binding.countsTextView.setText(alias.getCreationSpannableString(context), TextView.BufferType.SPANNABLE)

        binding.enabledSwitch.isChecked = alias.enabled
        if (alias.enabled) {
            binding.rootRelativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
        } else {
            binding.rootRelativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorMediumGray))
        }

        binding.noteTextView.text = alias.note
        binding.noteTextView.visibility = if (alias.note != null) View.VISIBLE else View.GONE
    }
}