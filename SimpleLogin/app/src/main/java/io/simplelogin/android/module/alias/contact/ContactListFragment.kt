package io.simplelogin.android.module.alias.contact

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.simplelogin.android.R
import io.simplelogin.android.databinding.FragmentContactListBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.SwipeToDeleteCallback
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.*
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.utils.model.Contact

class ContactListFragment : BaseFragment(), HomeActivity.OnBackPressed,
    Toolbar.OnMenuItemClickListener {
    private lateinit var binding: FragmentContactListBinding
    private lateinit var alias: Alias
    private lateinit var viewModel: ContactListViewModel
    private lateinit var adapter: ContactListAdapter
    private lateinit var howToBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var createContactBottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Binding
        binding = FragmentContactListBinding.inflate(layoutInflater)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.setOnMenuItemClickListener(this)
        alias = ContactListFragmentArgs.fromBundle(requireArguments()).alias

        binding.emailTextField.text = alias.email
        binding.emailTextField.isSelected = true // to trigger marquee animation

        setUpHowToBottomSheet()
        setUpCreateContactBottomSheet()
        setUpViewModel()
        setUpRecyclerView()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // On configuration change, force trigger refresh recyclerView
        if (adapter.itemCount == 0 && viewModel.contacts.isNotEmpty()) {
            adapter.submitList(viewModel.contacts)
            updateUiBaseOnNumOfContacts()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.rootConstraintLayout.isEnabled = !loading
        if (loading) {
            binding.icebergImageView.visibility = View.GONE
            binding.instructionTextView.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.icebergImageView.visibility = View.VISIBLE
            binding.instructionTextView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun updateUiBaseOnNumOfContacts() {
        if (viewModel.contacts.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.icebergImageView.visibility = View.VISIBLE
            binding.instructionTextView.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.icebergImageView.visibility = View.GONE
            binding.instructionTextView.visibility = View.GONE
        }
    }

    private fun setUpHowToBottomSheet() {
        binding.howToBottomSheet.root.layoutParams.height =
            requireActivity().getScreenMetrics().heightPixels

        howToBottomSheetBehavior = BottomSheetBehavior.from(binding.howToBottomSheet.root)
        howToBottomSheetBehavior.hide()
        binding.howToBottomSheet.closeButton.setOnClickListener {
            howToBottomSheetBehavior.hide()
        }
        howToBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.dimView.alpha = slideOffset * 60 / 100
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

    private fun setUpCreateContactBottomSheet() {
        binding.createContactBottomSheet.root.layoutParams.height =
            requireActivity().getScreenMetrics().heightPixels
        binding.createContactBottomSheet.aliasTextView.text = alias.email

        createContactBottomSheetBehavior =
            BottomSheetBehavior.from(binding.createContactBottomSheet.root)
        createContactBottomSheetBehavior.hide()
        binding.createContactBottomSheet.cancelButton.setOnClickListener {
            createContactBottomSheetBehavior.hide()
        }

        createContactBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.dimView.alpha = slideOffset * 60 / 100
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.dimView.visibility = View.GONE
                        activity?.dismissKeyboard()
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.createContactBottomSheet.emailTextField.editText?.requestFocus()
                        activity?.showKeyboard()
                    }

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

        binding.createContactBottomSheet.emailTextField.editText?.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isValidEmail()) {
                    binding.createContactBottomSheet.createButton.isEnabled = true
                    binding.createContactBottomSheet.emailTextField.error = null
                } else {
                    binding.createContactBottomSheet.createButton.isEnabled = false
                    binding.createContactBottomSheet.emailTextField.error = "Invalid email address"
                }
            }
        })

        binding.createContactBottomSheet.createButton.setOnClickListener {
            val email = binding.createContactBottomSheet.emailTextField.editText?.text.toString()
            if (!email.isValidEmail()) return@setOnClickListener

            createContactBottomSheetBehavior.hide()
            activity?.dismissKeyboard()
            setLoading(true)
            viewModel.create(email)
        }
    }

    private fun setUpViewModel() {
        val tempViewModel: ContactListViewModel by viewModels {
            context?.let {
                ContactListViewModelFactory(it, alias)
            } ?: throw IllegalStateException("Context is null")
        }
        viewModel = tempViewModel
        viewModel.fetchContacts()
        setLoading(true)
        viewModel.eventHaveNewContacts.observe(viewLifecycleOwner, Observer { haveNewContacts ->
            activity?.runOnUiThread {
                setLoading(false)
                if (haveNewContacts) {
                    adapter.submitList(viewModel.contacts)
                }

                if (binding.swipeRefreshLayout.isRefreshing) {
                    context?.toastUpToDate()
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                updateUiBaseOnNumOfContacts()
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                setLoading(false)
                context?.toastError(error)
                viewModel.onHandleErrorComplete()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        })

        // Create contact
        viewModel.eventFinishCallingCreateContact.observe(
            viewLifecycleOwner,
            Observer { finishedCallingCreateContact ->
                if (finishedCallingCreateContact) {
                    setLoading(false)
                    viewModel.onHandleFinishCallingCreateContactComplete()
                }
            })

        viewModel.eventCreatedContact.observe(viewLifecycleOwner, Observer { createdContact ->
            if (createdContact != null) {
                context?.toastShortly("Created \"$createdContact\"")
                viewModel.refreshContacts()
                viewModel.onHandleCreatedContactComplete()
            }
        })

        // Delete contact
        viewModel.eventFinishCallingDeleteContact.observe(
            viewLifecycleOwner,
            Observer { finishedCallingDeleteContact ->
                if (finishedCallingDeleteContact) {
                    setLoading(false)
                    viewModel.onHandleFinishCallingDeleteContactComplete()
                }
            })

        viewModel.eventDeletedContact.observe(viewLifecycleOwner, Observer { deletedContact ->
            if (deletedContact != null) {
                context?.toastShortly("Deleted \"$deletedContact\"")
                viewModel.refreshContacts()
                viewModel.onHandleDeletedContactComplete()
            }
        })
    }

    private fun setUpRecyclerView() {
        adapter = ContactListAdapter(object : ContactListAdapter.ClickListener {
            override fun onClick(contact: Contact) {
                MaterialAlertDialogBuilder(context, R.style.SlAlertDialogTheme)
                    .setTitle("Send mail to \"${contact.email}\"")
                    .setItems(
                        arrayOf("Copy reverse-alias", "Begin composing with default email")
                    ) { _, itemIndex ->
                        when (itemIndex) {
                            0 -> {
                                activity?.copyToClipboard(
                                    contact.reverseAlias,
                                    contact.reverseAlias
                                )
                                context?.toastShortly("Copied \"${contact.reverseAlias}\"")
                            }

                            1 -> activity?.startSendEmailIntent(contact.reverseAlias)
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
                if ((linearLayoutManager.findLastCompletelyVisibleItemPosition() == viewModel.contacts.size - 1) && viewModel.moreToLoad) {
                    viewModel.fetchContacts()
                }
            }
        })

        // Add swipe recognizer to recyclerView
        val itemTouchHelper = ItemTouchHelper(object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val contact = viewModel.contacts[viewHolder.adapterPosition]
                MaterialAlertDialogBuilder(context)
                    .setTitle("Delete \"${contact.email}\"?")
                    .setMessage("\uD83D\uDED1 This operation is irreversible. Please confirm.")
                    .setNeutralButton("Cancel", null)
                    .setNegativeButton("Delete") { _, _ ->
                        setLoading(true)
                        viewModel.delete(contact)
                    }.setOnDismissListener {
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                    }
                    .show()
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refreshContacts() }
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        if (howToBottomSheetBehavior.isExpanded()) {
            howToBottomSheetBehavior.hide()
        } else if (createContactBottomSheetBehavior.isExpanded()) {
            createContactBottomSheetBehavior.hide()
        } else {
            findNavController().navigateUp()
        }
    }

    // Toolbar.OnMenuItemClickListener
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.addMenuItem -> {
                // Clear text and error state before showing the sheet
                binding.createContactBottomSheet.emailTextField.editText?.text = null
                binding.createContactBottomSheet.emailTextField.error = null
                binding.createContactBottomSheet.createButton.isEnabled = false
                createContactBottomSheetBehavior.expand()
            }
            R.id.howToMenuItem -> howToBottomSheetBehavior.expand()
        }

        return true
    }
}