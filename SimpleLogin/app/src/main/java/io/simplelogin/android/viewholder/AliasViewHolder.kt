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
import io.simplelogin.android.module.alias.AliasListAdapter
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

    fun bind(alias: Alias, clickListener: AliasListAdapter.ClickListener) {
        binding.emailTextView.text = alias.email
        binding.countsTextView.setText(alias.getCountSpannableString(context), TextView.BufferType.SPANNABLE)

        binding.enabledSwitch.isChecked = alias.enabled
        if (alias.enabled) {
            binding.rootRelativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
        } else {
            binding.rootRelativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorMediumGray))
        }

        binding.creationDateTextView.text = alias.getCreationString()

        binding.noteTextView.text = alias.note
        binding.noteTextView.visibility = if (alias.note != null) View.VISIBLE else View.GONE

        // Add click events
        binding.rootRelativeLayout.setOnClickListener { clickListener.onClick(alias) }
        binding.enabledSwitch.setOnCheckedChangeListener { _, isChecked -> clickListener.onSwitch(alias, isChecked) }
        binding.copyButton.setOnClickListener { clickListener.onCopy(alias) }
        binding.sendEmailButton.setOnClickListener { clickListener.onSendEmail(alias) }
        binding.deleteButton.setOnClickListener { clickListener.onDelete(alias, adapterPosition) }
    }
}