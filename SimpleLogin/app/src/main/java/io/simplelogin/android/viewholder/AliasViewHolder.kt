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
import io.simplelogin.android.utils.extension.setDrawableStart
import io.simplelogin.android.utils.model.Action
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

        binding.mailboxesTextView.setText(alias.getMailboxesString(context), TextView.BufferType.SPANNABLE)

        when (val latestActivityString = alias.getLatestActivityString()) {
            null -> {
                binding.creationDateTextView.setDrawableStart(R.drawable.ic_clock_16dp)
                binding.creationDateTextView.text = alias.getCreationString()
            }

            else -> {
                binding.creationDateTextView.text = latestActivityString

                val drawableRes = when (alias.latestActivity?.action) {
                    Action.REPLY -> R.drawable.ic_reply_16dp
                    Action.FORWARD -> R.drawable.ic_send_16dp
                    Action.BLOCK, Action.BOUNCED -> R.drawable.ic_block_16dp
                    else -> 0
                }

                binding.creationDateTextView.setDrawableStart(drawableRes)
            }
        }

        binding.nameTextView.text = alias.name
        binding.nameTextView.visibility = if (alias.name != null) View.VISIBLE else View.GONE

        binding.noteTextView.text = alias.note
        binding.noteTextView.visibility = if (alias.note != null) View.VISIBLE else View.GONE

        // Add click events
        binding.rootRelativeLayout.setOnClickListener { clickListener.onClick(alias) }
        binding.enabledSwitch.setOnClickListener { clickListener.onSwitch(alias, adapterPosition)  }
        binding.copyButton.setOnClickListener { clickListener.onCopy(alias) }
        binding.sendEmailButton.setOnClickListener { clickListener.onSendEmail(alias) }
    }
}