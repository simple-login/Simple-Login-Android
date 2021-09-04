package io.simplelogin.android.viewholder

import io.simplelogin.android.utils.model.AliasActivity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.R
import io.simplelogin.android.databinding.RecyclerItemAliasActivityBinding
import io.simplelogin.android.module.alias.activity.AliasActivityListAdapter
import io.simplelogin.android.utils.extension.setTint
import io.simplelogin.android.utils.model.Action

class AliasActivityViewHolder(private val binding: RecyclerItemAliasActivityBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): AliasActivityViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecyclerItemAliasActivityBinding.inflate(layoutInflater, parent, false)
            return AliasActivityViewHolder(binding)
        }
    }

    private val context = binding.root.context

    fun bind(activity: AliasActivity, clickListener: AliasActivityListAdapter.ClickListener) {
        binding.root.setOnClickListener { clickListener.onClick(activity) }
        binding.timeTextView.text = activity.getTimestampString()

        when (activity.action) {
            Action.FORWARD -> {
                binding.emailTextView.text = activity.from
                binding.iconImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_send_28dp
                    )
                )
                binding.iconImageView.setTint(R.color.colorPrimary)
            }

            Action.REPLY -> {
                binding.emailTextView.text = activity.to
                binding.iconImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_reply_24dp
                    )
                )
                binding.iconImageView.setTint(R.color.colorPrimary)
            }

            else -> {
                binding.emailTextView.text = activity.from
                binding.iconImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_block_24dp
                    )
                )
                binding.iconImageView.setTint(R.color.colorNegative)
            }
        }
    }
}
