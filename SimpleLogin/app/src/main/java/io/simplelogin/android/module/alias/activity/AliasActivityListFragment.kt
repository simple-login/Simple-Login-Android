package io.simplelogin.android.module.alias.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.simplelogin.android.R
import io.simplelogin.android.databinding.DialogViewEditTextBinding
import io.simplelogin.android.databinding.FragmentAliasActivityBinding
import io.simplelogin.android.module.alias.AliasListViewModel
import io.simplelogin.android.module.alias.contact.ContactListFragmentArgs
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.*
import io.simplelogin.android.utils.model.Action
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.utils.model.AliasActivity

class AliasActivityListFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentAliasActivityBinding
    private val aliasListViewModel: AliasListViewModel by activityViewModels()
    private lateinit var viewModel: AliasActivityListViewModel
    private lateinit var adapter: AliasActivityListAdapter
    private lateinit var alias: Alias
    private val addOrEditString: String
        get() {
            return if (alias.note != null && alias.note != "") "Edit note" else "Add note"
        }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAliasActivityBinding.inflate(inflater)

        binding.toolbar.setNavigationOnClickListener { updateAliasListViewModelAndNavigateUp() }
        alias = ContactListFragmentArgs.fromBundle(requireArguments()).alias
        binding.toolbarTitleText.text = alias.email
        binding.toolbarTitleText.isSelected = true // to trigger marquee animation

        // Bind create date
        binding.creationDateTextView.text = alias.getPreciseCreationString()
        updateNote()

        binding.editNoteButton.setOnClickListener {
            val dialogTextViewBinding = DialogViewEditTextBinding.inflate(layoutInflater)
            dialogTextViewBinding.editText.setText(alias.note)
            MaterialAlertDialogBuilder(context)
                .setTitle(addOrEditString)
                .setMessage(alias.email)
                .setView(dialogTextViewBinding.root)
                .setNeutralButton("Cancel", null)
                .setPositiveButton("Update") { _, _ ->
                    viewModel.updateNote(dialogTextViewBinding.editText.text.toString(), firebaseAnalytics)
                }
                .show()

            dialogTextViewBinding.editText.requestFocus()
        }

        setUpStats()
        setUpViewModel()
        setUpRecyclerView()

        firebaseAnalytics.logEvent("open_alias_activity_list_fragment", null)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setUpStats() {
        // Handled
        binding.handledStat.root.makeSubviewsClippedToBound()
        binding.handledStat.iconImageView.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_at_48dp
            )
        )
        binding.handledStat.numberTextView.text = "${alias.handleCount}"
        binding.handledStat.typeTextView.text = "Email handled"

        // Forwarded
        binding.forwardedStat.root.makeSubviewsClippedToBound()
        binding.forwardedStat.iconImageView.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_send_48dp
            )
        )
        binding.forwardedStat.numberTextView.text = "${alias.forwardCount}"
        binding.forwardedStat.typeTextView.text = "Email forwarded"

        // Reply
        binding.repliedStat.root.makeSubviewsClippedToBound()
        binding.repliedStat.iconImageView.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_reply_48dp
            )
        )
        binding.repliedStat.numberTextView.text = "${alias.replyCount}"
        binding.repliedStat.typeTextView.text = "Email replied"

        // Block
        binding.blockedStat.root.makeSubviewsClippedToBound()
        binding.blockedStat.iconImageView.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_block_48dp
            )
        )
        binding.blockedStat.rootLinearLayout.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorNegative
            )
        )
        binding.blockedStat.numberTextView.text = "${alias.blockCount}"
        binding.blockedStat.typeTextView.text = "Email blocked"
    }

    @SuppressLint("SetTextI18n")
    private fun updateNote() {
        if (alias.note != null) {
            binding.noteTextView.text = alias.note
        } else {
            binding.noteTextView.text = "Add some note for this alias"
        }
        binding.editNoteButton.text = addOrEditString
    }

    private fun setUpViewModel() {
        val tempViewModel: AliasActivityListViewModel by viewModels {
            context?.let {
                AliasActivityListViewModelFactory(it, alias)
            } ?: throw IllegalStateException("Context is null")
        }
        viewModel = tempViewModel
        viewModel.fetchActivities(firebaseAnalytics)
        viewModel.eventHaveNewActivities.observe(viewLifecycleOwner, Observer { haveNewActivities ->
            activity?.runOnUiThread {
                if (haveNewActivities) {
                    adapter.submitList(viewModel.activities)
                }

                if (binding.swipeRefreshLayout.isRefreshing) {
                    context?.toastUpToDate()
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                context?.toastError(error)
                viewModel.onHandleErrorComplete()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        })

        viewModel.eventNoteUpdate.observe(viewLifecycleOwner, Observer { noteUpdated ->
            if (noteUpdated) {
                updateNote()
                viewModel.onHandleNoteUpdateComplete()
            }
        })
    }

    private fun setUpRecyclerView() {
        adapter = AliasActivityListAdapter(object : AliasActivityListAdapter.ClickListener {
            override fun onClick(aliasActivity: AliasActivity) {
                val toEmail = when (aliasActivity.action) {
                    Action.REPLY -> aliasActivity.to
                    else -> aliasActivity.from
                }

                MaterialAlertDialogBuilder(context, R.style.SlAlertDialogTheme)
                    .setTitle("Email to \"$toEmail\"")
                    .setItems(
                        arrayOf("Copy reverse-alias", "Begin composing with default email")
                    ) { _, itemIndex ->
                        when (itemIndex) {
                            0 -> {
                                activity?.copyToClipboard(
                                    aliasActivity.reverseAlias,
                                    aliasActivity.reverseAlias
                                )
                                context?.toastShortly("Copied \"${aliasActivity.reverseAlias}\"")
                                firebaseAnalytics.logEvent("activity_copy_reverse_alias", null)
                            }

                            1 -> {
                                activity?.startSendEmailIntent(aliasActivity.reverseAlias)
                                firebaseAnalytics.logEvent("activity_compose_email", null)
                            }
                        }
                    }
                    .show()
            }
        })
        binding.recyclerView.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = linearLayoutManager

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if ((linearLayoutManager.findLastCompletelyVisibleItemPosition() == viewModel.activities.size - 1) && viewModel.moreToLoad) {
                    viewModel.fetchActivities(firebaseAnalytics)
                    firebaseAnalytics.logEvent("alias_activity_fetch_more", null)
                }
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshAlias()
            viewModel.refreshActivities(firebaseAnalytics)
            firebaseAnalytics.logEvent("alias_activity_refresh", null)
        }
    }

    private fun refreshAlias() {
        SLApiService.getAlias(viewModel.apiKey, alias.id) { alias, error ->
            activity?.runOnUiThread {
                if (error != null) {
                    context?.toastError(error)
                } else if (alias != null) {
                    this.alias = alias
                    setUpStats()
                }
            }
        }
    }

    private fun updateAliasListViewModelAndNavigateUp() {
        aliasListViewModel.updateAlias(alias)
        findNavController().navigateUp()
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        updateAliasListViewModelAndNavigateUp()
    }
}