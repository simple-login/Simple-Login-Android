package io.simplelogin.android.module.alias.create

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.simplelogin.android.databinding.FragmentAliasCreateBinding
import io.simplelogin.android.module.alias.AliasListViewModel
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.*
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.utils.model.toSpannableString

class AliasCreateFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentAliasCreateBinding
    private val aliasListViewModel: AliasListViewModel by activityViewModels()
    private var selectedSuffix: String? = null
    private lateinit var viewModel: AliasCreateViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAliasCreateBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { dismissKeyboardAndNavigateUp() }

        // Enable/disable createButton
        binding.prefixEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.createButton.isEnabled = s?.toString()?.isValidEmailPrefix() ?: false
            }
        })

        binding.createButton.setOnClickListener {
            if (selectedSuffix == null) {
                context?.toastShortly("No suffix is selected")
                return@setOnClickListener
            }
            createAlias()
        }

        binding.mailboxesTitleLinearLayout.setOnClickListener { showSelectMailboxesAlert() }
        binding.mailboxesTextView.setOnClickListener { showSelectMailboxesAlert() }

        binding.root.setOnClickListener { activity?.dismissKeyboard() }

        // viewModel
        viewModel = AliasCreateViewModel(requireContext())

        setLoading(true)
        viewModel.fetchUserOptionsAndMailboxes()

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                context?.toastError(error)
                findNavController().navigateUp()
            }
        }

        viewModel.userOptions.observe(viewLifecycleOwner) { userOptions ->
            if (userOptions != null) {
                setLoading(false)

                if (userOptions.canCreate) {
                    setUpSuffixesSpinner(userOptions.suffixes.map { it.suffix })
                } else {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Can not create more alias")
                        .setMessage("Go premium for unlimited aliases and more.")
                        .setPositiveButton("See pricing", null)
                        .setOnDismissListener {
                            aliasListViewModel.setNeedsSeePricing()
                            findNavController().navigateUp()
                        }
                        .show()
                }
            }
        }

        viewModel.selectedMailboxes.observe(viewLifecycleOwner) { selectedMailboxes ->
            if (selectedMailboxes != null) {
                setLoading(false)
                binding.mailboxesTextView.setText(
                    selectedMailboxes.toSpannableString(requireContext()),
                    TextView.BufferType.SPANNABLE
                )
            }
        }

        return binding.root
    }

    private fun dismissKeyboardAndNavigateUp() {
        activity?.dismissKeyboard()
        findNavController().navigateUp()
    }

    private fun updateAliasListViewModelAndNavigateUp(alias: Alias) {
        if (AliasCreateFragmentArgs.fromBundle(requireArguments()).isMailFromAlias) {
            aliasListViewModel.setMailFromAlias(alias)
        } else {
            context?.toastShortly("Created \"${alias.email}\"")
        }
        aliasListViewModel.addAlias(alias)
        dismissKeyboardAndNavigateUp()
    }

    private fun setUpSuffixesSpinner(suffixes: List<String>) {
        binding.suffixesSpinner.adapter = AliasCreateSpinnerAdapter(requireContext(), suffixes)
        binding.suffixesSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedSuffix = suffixes[position]
                }
            }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            binding.rootLinearLayout.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.rootLinearLayout.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun createAlias() {
        val apiKey = SLSharedPreferences.getApiKey(requireContext()) ?: throw IllegalStateException(
            "API key is null"
        )

        val signedSuffix = viewModel.userOptions.value!!.suffixes.first { it.suffix == selectedSuffix }.signedSuffix
        setLoading(true)

        SLApiService.createAlias(
            apiKey,
            binding.prefixEditText.text.toString(),
            signedSuffix,
            viewModel.selectedMailboxes.value!!.map { it.id },
            binding.nameEditText.text.toString(),
            binding.noteEditText.text.toString()
        ) { result ->
            activity?.runOnUiThread {
                setLoading(false)
                result.onSuccess(::updateAliasListViewModelAndNavigateUp)
                result.onFailure { context?.toastThrowable(it) }
            }
        }
    }

    private fun showSelectMailboxesAlert() {
        viewModel.selectedMailboxes.value?.let { selectedMailboxes ->
            activity?.showSelectMailboxesAlert(
                viewModel.mailboxes,
                selectedMailboxes
            ) { checkedMailboxes ->
                viewModel.setSelectedMailboxes(checkedMailboxes)
            }
        }
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        dismissKeyboardAndNavigateUp()
    }
}
