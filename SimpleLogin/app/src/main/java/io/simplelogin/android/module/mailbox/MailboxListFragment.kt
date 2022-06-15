package io.simplelogin.android.module.mailbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.simplelogin.android.R
import io.simplelogin.android.databinding.DialogViewEditTextBinding
import io.simplelogin.android.databinding.FragmentMailboxListBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.SwipeHelper
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.*

class MailboxListFragment : BaseFragment(), HomeActivity.OnBackPressed,
    Toolbar.OnMenuItemClickListener {
    companion object {
        private const val BOTTOM_SHEET_HEIGHT_PERCENTAGE_TO_SCREEN_HEIGHT = 90.0f / 100
        private const val DIM_VIEW_ALPHA_PERCENTAGE_TO_SLIDE_OFFSET = 60.0f / 100
    }

    private lateinit var binding: FragmentMailboxListBinding
    private lateinit var viewModel: MailboxListViewModel
    private var itemTouchHelper: ItemTouchHelper? = null
    private lateinit var adapter: MailboxListAdapter
    private lateinit var howToUseMailboxBehavior: BottomSheetBehavior<View>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setUpBinding()
        setUpHowToUseMailboxBottomSheet()
        setUpViewModel()
        setUpRecyclerView()

        setLoading(true)
        viewModel.fetchMailboxes()

        return binding.root
    }

    private fun setLoading(loading: Boolean) {
        binding.rootConstraintLayout.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setUpBinding() {
        binding = FragmentMailboxListBinding.inflate(layoutInflater)
        binding.toolbar.setNavigationOnClickListener { showLeftMenu() }
        binding.toolbar.setOnMenuItemClickListener(this)
    }

    private fun setUpHowToUseMailboxBottomSheet() {
        binding.howToUseMailboxBottomSheet.root.layoutParams.height =
            (requireActivity().getScreenHeight() * BOTTOM_SHEET_HEIGHT_PERCENTAGE_TO_SCREEN_HEIGHT).toInt()

        howToUseMailboxBehavior =
            BottomSheetBehavior.from(binding.howToUseMailboxBottomSheet.root)
        howToUseMailboxBehavior.hide()
        binding.howToUseMailboxBottomSheet.closeButton.setOnClickListener { howToUseMailboxBehavior.hide() }

        howToUseMailboxBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.dimView.alpha = slideOffset * DIM_VIEW_ALPHA_PERCENTAGE_TO_SLIDE_OFFSET
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> binding.dimView.visibility = View.GONE

                    else -> {
                        binding.dimView.visibility = View.VISIBLE
                        binding.dimView.setOnTouchListener { _, _ ->
                            // Must return true here to intercept touch event
                            // if not the event is passed to next listener which cause the whole root is clickable
                            true
                        }
                    }
                }
            }
        })
    }

    private fun setUpViewModel() {
        viewModel = MailboxListViewModel(requireContext())

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                setLoading(false)
                context?.toastError(error)
                viewModel.onHandleErrorComplete()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        viewModel.eventUpdateMailboxes.observe(viewLifecycleOwner) { haveNewMailboxes ->
            activity?.runOnUiThread {
                setLoading(false)

                if (haveNewMailboxes) {
                    // toMutableList() is required. Refer to AliasListFragment viewModel
                    adapter.submitList(viewModel.mailboxes.toMutableList())
                    setUpItemTouchHelper()
                    viewModel.onHandleUpdateMailboxesComplete()
                }

                if (binding.swipeRefreshLayout.isRefreshing) {
                    context?.toastUpToDate()
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        viewModel.createdMailbox.observe(viewLifecycleOwner) { createdMailbox ->
            activity?.runOnUiThread {
                setLoading(false)
                if (createdMailbox != null) {
                    context?.toastLongly("You are going to receive a confirmation email for $createdMailbox")
                    viewModel.onHandleCreatedMailboxComplete()
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        adapter = MailboxListAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.fetchMailboxes() }
    }

    private fun setUpItemTouchHelper() {
        itemTouchHelper?.attachToRecyclerView(null)

        itemTouchHelper = ItemTouchHelper(object : SwipeHelper(binding.recyclerView) {
            override fun instantiateUnderlayButton(position: Int): List<UnderlayButton> {
                when (viewModel.mailboxes[position].isDefault) {
                    true -> return emptyList()
                    false -> {
                        val deleteButton = UnderlayButton(
                            requireContext(),
                            "Delete",
                            UnderlayButton.DEFAULT_TEXT_SIZE,
                            android.R.color.holo_red_light,
                            object : UnderlayButtonClickListener {
                                override fun onClick() {
                                    confirmDelete(position)
                                }
                            })

                        val setAsDefaultButton = UnderlayButton(
                            requireContext(),
                            "Set as default",
                            UnderlayButton.DEFAULT_TEXT_SIZE,
                            android.R.color.holo_blue_light,
                            object : UnderlayButtonClickListener {
                                override fun onClick() {
                                    confirmSetAsDefault(position)
                                }
                            })

                        return listOf(deleteButton, setAsDefaultButton)
                    }
                }
            }
        })

        itemTouchHelper?.attachToRecyclerView(binding.recyclerView)
    }

    private fun confirmDelete(position: Int) {
        val mailbox = viewModel.mailboxes[position]
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete \"${mailbox.email}\"")
            .setMessage(R.string.warning_before_deleting_mailbox)
            .setNegativeButton("Delete") { _, _ ->
                setLoading(true)
                viewModel.deleteMailbox(mailbox)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun confirmSetAsDefault(position: Int) {
        val mailbox = viewModel.mailboxes[position]
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Please confirm")
            .setMessage("Make \"${mailbox.email}\" default mailbox?")
            .setPositiveButton("Confirm") { _, _ ->
                setLoading(true)
                viewModel.makeDefault(mailbox)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        when {
            howToUseMailboxBehavior.isExpanded() -> howToUseMailboxBehavior.hide()
            else -> findNavController().navigateUp()
        }
    }

    // Toolbar.OnMenuItemClickListener
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.addMenuItem -> {
                val dialogTextViewBinding = DialogViewEditTextBinding.inflate(layoutInflater)
                dialogTextViewBinding.editText.hint = "my-another-email@example.com"
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("New mailbox")
                    .setMessage("A verification email will be sent to this email address")
                    .setView(dialogTextViewBinding.root)
                    .setNeutralButton("Cancel", null)
                    .setPositiveButton("Create") { _, _ ->
                        setLoading(true)
                        viewModel.create(dialogTextViewBinding.editText.text.toString())
                    }
                    .show()
            }

            R.id.howToMenuItem -> howToUseMailboxBehavior.expand()
        }

        return true
    }
}
