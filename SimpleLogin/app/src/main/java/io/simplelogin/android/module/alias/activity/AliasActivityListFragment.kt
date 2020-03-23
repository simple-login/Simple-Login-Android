package io.simplelogin.android.module.alias.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.R
import io.simplelogin.android.databinding.FragmentAliasActivityBinding
import io.simplelogin.android.module.alias.contact.ContactListFragmentArgs
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.makeSubviewsClippedToBound
import io.simplelogin.android.utils.model.Alias

class AliasActivityListFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentAliasActivityBinding
    private lateinit var alias: Alias

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAliasActivityBinding.inflate(inflater)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        alias = ContactListFragmentArgs.fromBundle(requireArguments()).alias
        binding.toolbarTitleText.text = alias.email
        binding.toolbarTitleText.isSelected = true // to trigger marquee animation

        // Bind create date & note
        binding.creationDateTextView.text = alias.getPreciseCreationString()
        if (alias.note != null) {
            binding.noteTextView.text = alias.note
            binding.editNoteButton.text = "Edit note"
        } else {
            binding.noteTextView.text = "Add some note for this alias"
            binding.editNoteButton.text = "Add note"
        }

        setUpStats()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setUpStats() {
        // Handled
        binding.handledStat.root.makeSubviewsClippedToBound()
        binding.handledStat.iconImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_at_48dp))
        binding.handledStat.numberTextView.text = "${alias.handleCount}"
        binding.handledStat.typeTextView.text = "Email handled"

        // Forwarded
        binding.forwardedStat.root.makeSubviewsClippedToBound()
        binding.forwardedStat.iconImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_send_48dp))
        binding.forwardedStat.numberTextView.text = "${alias.forwardCount}"
        binding.forwardedStat.typeTextView.text = "Email forwarded"

        // Reply
        binding.repliedStat.root.makeSubviewsClippedToBound()
        binding.repliedStat.iconImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_reply_48dp))
        binding.repliedStat.numberTextView.text = "${alias.replyCount}"
        binding.repliedStat.typeTextView.text = "Email replied"

        // Block
        binding.blockedStat.root.makeSubviewsClippedToBound()
        binding.blockedStat.iconImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_block_48dp))
        binding.blockedStat.rootLinearLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
        binding.blockedStat.numberTextView.text = "${alias.blockCount}"
        binding.blockedStat.typeTextView.text = "Email blocked"
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        findNavController().navigateUp()
    }
}