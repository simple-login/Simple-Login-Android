package io.simplelogin.android.module.alias.create

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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

class AliasCreateFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentAliasCreateBinding
    private val aliasListViewModel: AliasListViewModel by activityViewModels()
    private var selectedSuffix: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAliasCreateBinding.inflate(inflater)

        binding.toolbar.setNavigationOnClickListener { dismissKeyboardAndNavigateUp() }

        val apiKey = SLSharedPreferences.getApiKey(requireContext()) ?: throw IllegalStateException(
            "API key is null"
        )

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

            val prefix = binding.prefixEditText.text.toString()
            val note = binding.noteTextField.editText?.text.toString()
            createAlias(apiKey, prefix, selectedSuffix!!, note)
        }

        // Fetch UserOptions
        setLoading(true)
        SLApiService.fetchUserOptions(apiKey) { userOptions, error ->
            activity?.runOnUiThread {
                setLoading(false)
                if (error != null) {
                    context?.toastError(error)
                    findNavController().navigateUp()
                } else if (userOptions != null) {
                    if (userOptions.canCreate) {
                        setUpSuffixesSpinner(userOptions.suffixes)
                    } else {
                        MaterialAlertDialogBuilder(context)
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
        }

        firebaseAnalytics.logEvent("open_alias_create_fragment", null)

        return binding.root
    }

    private fun dismissKeyboardAndNavigateUp() {
        activity?.dismissKeyboard()
        findNavController().navigateUp()
    }

    private fun updateAliasListViewModelAndNavigateUp(alias: Alias) {
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
                    firebaseAnalytics.logEvent("alias_create_select_suffix", null)
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

    private fun createAlias(apiKey: String, prefix: String, suffix: String, note: String?) {
        setLoading(true)
        SLApiService.createAlias(apiKey, prefix, suffix, note) { alias, error ->
            activity?.runOnUiThread {
                setLoading(false)
                if (error != null) {
                    context?.toastError(error)
                    firebaseAnalytics.logEvent("create_alias_error", error.toBundle())
                } else if (alias != null) {
                    updateAliasListViewModelAndNavigateUp(alias)
                    context?.toastShortly("Created \"${alias.email}\"")
                    firebaseAnalytics.logEvent("create_alias_success", null)
                }
            }
        }
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        dismissKeyboardAndNavigateUp()
    }
}
