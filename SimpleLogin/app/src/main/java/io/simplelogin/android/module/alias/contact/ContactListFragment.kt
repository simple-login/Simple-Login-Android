package io.simplelogin.android.module.alias.contact

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.simplelogin.android.R
import io.simplelogin.android.databinding.FragmentContactListBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.LoadingFooterAdapter
import io.simplelogin.android.utils.SwipeHelper
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.*
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.utils.model.Contact
import io.simplelogin.android.utils.model.PickedEmail

class ContactListFragment : BaseFragment(), HomeActivity.OnBackPressed,
    Toolbar.OnMenuItemClickListener {
    companion object {
        private const val BOTTOM_SHEET_HEIGHT_PERCENTAGE_TO_SCREEN_HEIGHT = 90.0f / 100
        private const val DIM_VIEW_ALPHA_PERCENTAGE_TO_SLIDE_OFFSET = 60.0f / 100
        private const val RC_CONTACTS_ACCESS = 1000
    }

    private lateinit var binding: FragmentContactListBinding
    private lateinit var alias: Alias
    private lateinit var viewModel: ContactListViewModel
    private lateinit var contactListAdapter: ContactListAdapter
    private lateinit var howToBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var createContactBottomSheetBehavior: BottomSheetBehavior<View>
    private val footerAdapter = LoadingFooterAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        if (contactListAdapter.itemCount == 0 && viewModel.contacts.isNotEmpty()) {
            contactListAdapter.submitList(viewModel.contacts)
            updateUiBaseOnNumOfContacts()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.rootConstraintLayout.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.icebergImageView.visibility =
            if (loading) View.GONE else if (viewModel.contacts.isEmpty()) View.VISIBLE else View.GONE
        binding.instructionTextView.visibility = binding.icebergImageView.visibility
    }

    private fun showLoadingFooter(showing: Boolean) {
        footerAdapter.isLoading = showing
        footerAdapter.notifyDataSetChanged()
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
            (requireActivity().getScreenHeight() * BOTTOM_SHEET_HEIGHT_PERCENTAGE_TO_SCREEN_HEIGHT).toInt()

        howToBottomSheetBehavior = BottomSheetBehavior.from(binding.howToBottomSheet.root)
        howToBottomSheetBehavior.hide()
        binding.howToBottomSheet.closeButton.setOnClickListener {
            howToBottomSheetBehavior.hide()
        }
        howToBottomSheetBehavior.addBottomSheetCallback(object :
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

    private fun setUpCreateContactBottomSheet() {
        binding.createContactBottomSheet.root.layoutParams.height =
            (requireActivity().getScreenHeight() * BOTTOM_SHEET_HEIGHT_PERCENTAGE_TO_SCREEN_HEIGHT).toInt()
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
                binding.dimView.alpha = slideOffset * DIM_VIEW_ALPHA_PERCENTAGE_TO_SLIDE_OFFSET
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.dimView.visibility = View.GONE
                        activity?.dismissKeyboard()
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.createContactBottomSheet.contactEmailTextField.editText?.requestFocus()
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

        binding.createContactBottomSheet.contactEmailTextField.editText?.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isValidEmail()) {
                    binding.createContactBottomSheet.createButton.isEnabled = true
                    binding.createContactBottomSheet.contactEmailTextField.error = null
                } else {
                    binding.createContactBottomSheet.createButton.isEnabled = false
                    binding.createContactBottomSheet.contactEmailTextField.error = "Invalid email address"
                }
            }
        })

        binding.createContactBottomSheet.createButton.setOnClickListener {
            val email = binding.createContactBottomSheet.contactEmailTextField.editText?.text.toString()
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
        viewModel.eventHaveNewContacts.observe(viewLifecycleOwner) { haveNewContacts ->
            activity?.runOnUiThread {
                setLoading(false)
                showLoadingFooter(false)
                if (haveNewContacts) {
                    contactListAdapter.submitList(viewModel.contacts.toMutableList())
                }

                if (binding.swipeRefreshLayout.isRefreshing) {
                    context?.toastUpToDate()
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                updateUiBaseOnNumOfContacts()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                setLoading(false)
                context?.toastError(error)
                viewModel.onHandleErrorComplete()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        // Create contact
        viewModel.eventFinishCallingCreateContact.observe(
            viewLifecycleOwner
        ) { finishedCallingCreateContact ->
            if (finishedCallingCreateContact) {
                setLoading(false)
                viewModel.onHandleFinishCallingCreateContactComplete()
            }
        }

        viewModel.eventCreatedContact.observe(viewLifecycleOwner) { createdContact ->
            if (createdContact != null) {
                context?.toastShortly("Created \"$createdContact\"")
                viewModel.refreshContacts()
                viewModel.onHandleCreatedContactComplete()
            }
        }

        // Delete contact
        viewModel.eventFinishCallingDeleteContact.observe(
            viewLifecycleOwner
        ) { finishedCallingDeleteContact ->
            if (finishedCallingDeleteContact) {
                setLoading(false)
                viewModel.onHandleFinishCallingDeleteContactComplete()
            }
        }

        viewModel.eventDeletedContact.observe(viewLifecycleOwner) { deletedContact ->
            if (deletedContact != null) {
                context?.toastShortly("Deleted \"$deletedContact\"")
                viewModel.refreshContacts()
                viewModel.onHandleDeletedContactComplete()
            }
        }

        // Toggle contact
        viewModel.eventFinishTogglingContact.observe(viewLifecycleOwner) { finishTogglingContact ->
            if (finishTogglingContact) {
                setLoading(false)
                contactListAdapter.notifyDataSetChanged()
                viewModel.onHandleToggledContactComplete()
            }
        }
    }

    private fun setUpRecyclerView() {
        contactListAdapter = ContactListAdapter(object : ContactListAdapter.ClickListener {
            override fun onClick(contact: Contact) {
                alertContactOptions(contact)
            }
        })
        binding.recyclerView.adapter = ConcatAdapter(contactListAdapter, footerAdapter)
        val linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = linearLayoutManager

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val isPenultimateItem =
                    linearLayoutManager.findLastCompletelyVisibleItemPosition() == viewModel.contacts.size - 1
                if (isPenultimateItem && viewModel.moreToLoad) {
                    showLoadingFooter(true)
                    viewModel.fetchContacts()
                }
            }
        })

        // Add swipe recognizer to recyclerView
        val itemTouchHelper = ItemTouchHelper(object : SwipeHelper(binding.recyclerView) {
            override fun instantiateUnderlayButton(position: Int): List<UnderlayButton> {
                return listOf(
                    UnderlayButton(
                        requireContext(),
                        "Delete",
                        UnderlayButton.DEFAULT_TEXT_SIZE,
                        android.R.color.holo_red_light,
                        object : UnderlayButtonClickListener {
                            override fun onClick() {
                                val contact = viewModel.contacts[position]
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Delete \"${contact.email}\"?")
                                    .setMessage("\uD83D\uDED1 This operation is irreversible. Please confirm.")
                                    .setNeutralButton("Cancel", null)
                                    .setNegativeButton("Delete") { _, _ ->
                                        setLoading(true)
                                        viewModel.delete(contact)
                                    }
                                    .show()
                            }
                        })
                )
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshContacts()
        }
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        when {
            howToBottomSheetBehavior.isExpanded() -> howToBottomSheetBehavior.hide()
            createContactBottomSheetBehavior.isExpanded() -> createContactBottomSheetBehavior.hide()
            else -> findNavController().navigateUp()
        }
    }

    // Toolbar.OnMenuItemClickListener
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.addMenuItem -> {
                if (context?.canReadContacts() == true) {
                    alertCreationOptions()
                } else {
                    showCreateContactBottomSheet()
                }
            }
            R.id.howToMenuItem -> howToBottomSheetBehavior.expand()
        }
        return true
    }

    private fun alertCreationOptions() {
        MaterialAlertDialogBuilder(requireContext(), R.style.SlAlertDialogTheme)
            .setTitle("Create new contact")
            .setItems(
                arrayOf("Open phone contacts", "Manually enter email address")
            ) { _, itemIndex ->
                when (itemIndex) {
                    0 -> openPhoneContacts()
                    1 -> showCreateContactBottomSheet()
                }
            }
            .show()
    }

    private fun openPhoneContacts() {
        val contactsIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(contactsIntent, RC_CONTACTS_ACCESS)
    }

    private fun showCreateContactBottomSheet() {
        // Clear text and error state before showing the sheet
        binding.createContactBottomSheet.contactEmailTextField.editText?.text = null
        binding.createContactBottomSheet.contactEmailTextField.error = null
        binding.createContactBottomSheet.createButton.isEnabled = false
        createContactBottomSheetBehavior.expand()
    }

    private fun showPickedEmailAddresses(contactName: String, emails: List<PickedEmail>) {
        MaterialAlertDialogBuilder(requireContext(), R.style.SlAlertDialogTheme)
            .setTitle(contactName)
            .setItems(emails.map { it.description }.toTypedArray()) { _, itemIndex ->
                val email = emails[itemIndex]
                if (email.address.isValidEmail()) {
                    viewModel.create(email.address)
                } else {
                    context?.toastShortly("Invalid email address: ${email.address}")
                }
            }
            .show()
    }

    private fun alertContactOptions(contact: Contact) {
        fun copyToClipboardAndToast(text: String) {
            copyToClipboard(text, text)
            context?.toastShortly("Copied $text")
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.SlAlertDialogTheme)
            .setTitle(contact.email)
            .setItems(
                arrayOf(
                    getString(R.string.copy_reverse_alias_with_display_name),
                    getString(R.string.copy_reverse_alias_without_display_name),
                    getString(R.string.begin_composing_with_default_email),
                    if (contact.blockForward) "Unblock" else "Block"
                )
            ) { _, itemIndex ->
                when (itemIndex) {
                    0 -> copyToClipboardAndToast(contact.reverseAlias)
                    1 -> copyToClipboardAndToast(contact.reverseAliasAddress)
                    2 -> activity?.startSendEmailIntent(contact.reverseAlias)
                    3 -> {
                        setLoading(true)
                        viewModel.toggle(contact)
                    }
                }
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == RC_CONTACTS_ACCESS) {
            val contactData = data?.data ?: return
            val contentResolver = activity?.contentResolver ?: return
            val cursor = contentResolver.query(contactData, null, null, null, null) ?: return
            if (!cursor.moveToFirst()) return
            val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            val emailsCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,
                null, null
            ) ?: return
            val pickedEmails = mutableListOf<PickedEmail>()
            while (emailsCursor.moveToNext()) {
                val email = PickedEmail(emailsCursor)
                pickedEmails.add(email)
            }
            if (pickedEmails.isEmpty()) {
                context?.toastShortly("This contact has no email address")
            } else {
                showPickedEmailAddresses(name, pickedEmails)
            }
        }
    }
}
