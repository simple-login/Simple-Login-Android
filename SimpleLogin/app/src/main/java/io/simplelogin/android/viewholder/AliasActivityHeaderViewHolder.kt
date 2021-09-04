package io.simplelogin.android.viewholder

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.R
import io.simplelogin.android.databinding.RecyclerItemAliasActivityHeaderBinding
import io.simplelogin.android.module.alias.activity.AliasActivityListHeaderAdapter
import io.simplelogin.android.utils.extension.makeSubviewsClippedToBound
import io.simplelogin.android.utils.model.Alias

class AliasActivityHeaderViewHolder(private val binding: RecyclerItemAliasActivityHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): AliasActivityHeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding =
                RecyclerItemAliasActivityHeaderBinding.inflate(layoutInflater, parent, false)
            return AliasActivityHeaderViewHolder(binding)
        }
    }

    private val context = binding.root.context

    @SuppressLint("SetTextI18n")
    fun bind(alias: Alias, clickListener: AliasActivityListHeaderAdapter.ClickListener) {
        binding.creationDateTextView.text = alias.getPreciseCreationString()

        binding.editMailboxesButton.setOnClickListener { clickListener.editMailboxesButtonClicked() }
        binding.mailboxesTextView.setText(
            alias.getMailboxesString(context),
            TextView.BufferType.SPANNABLE
        )

        binding.editNameButton.setOnClickListener { clickListener.editNameButtonClicked() }
        if (alias.name != null) {
            binding.nameTextView.text = alias.name
            binding.nameTextView.setTypeface(null, Typeface.NORMAL)
            binding.editNameButton.text = "Edit name"
        } else {
            binding.nameTextView.text = "<No display name>"
            binding.nameTextView.setTypeface(null, Typeface.ITALIC)
            binding.editNameButton.text = "Add name"
        }

        binding.editNoteButton.setOnClickListener { clickListener.editNoteButtonClicked() }
        if (alias.note != null) {
            binding.noteTextView.text = alias.note
            binding.noteTextView.setTypeface(null, Typeface.NORMAL)
            binding.editNoteButton.text = "Edit note"
        } else {
            binding.noteTextView.text = "<No note>"
            binding.noteTextView.setTypeface(null, Typeface.ITALIC)
            binding.editNoteButton.text = "Add note"
        }

        setUpStats(alias)
    }

    @SuppressLint("SetTextI18n")
    private fun setUpStats(alias: Alias) {
        // Handled
        binding.handledStat.root.makeSubviewsClippedToBound()
        binding.handledStat.iconImageView.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_at_58dp)
        )
        binding.handledStat.numberTextView.text = "${alias.handleCount}"
        binding.handledStat.typeTextView.text = "Email handled"

        // Forwarded
        binding.forwardedStat.root.makeSubviewsClippedToBound()
        binding.forwardedStat.iconImageView.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_send_48dp)
        )
        binding.forwardedStat.numberTextView.text = "${alias.forwardCount}"
        binding.forwardedStat.typeTextView.text = "Email forwarded"

        // Reply
        binding.repliedStat.root.makeSubviewsClippedToBound()
        binding.repliedStat.iconImageView.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_reply_58dp)
        )
        binding.repliedStat.numberTextView.text = "${alias.replyCount}"
        binding.repliedStat.typeTextView.text = "Email replied"

        // Block
        binding.blockedStat.root.makeSubviewsClippedToBound()
        binding.blockedStat.iconImageView.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_block_58dp)
        )
        binding.blockedStat.rootLinearLayout.setBackgroundColor(
            ContextCompat.getColor(context, R.color.colorNegative)
        )
        binding.blockedStat.numberTextView.text = "${alias.blockCount}"
        binding.blockedStat.typeTextView.text = "Email blocked"
    }
}
