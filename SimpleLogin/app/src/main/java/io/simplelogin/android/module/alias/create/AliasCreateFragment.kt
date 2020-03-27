package io.simplelogin.android.module.alias.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.databinding.FragmentAliasCreateBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.dismissKeyboard
import io.simplelogin.android.utils.extension.toastError
import io.simplelogin.android.utils.extension.toastLongly

class AliasCreateFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentAliasCreateBinding
    private var selectedSuffix: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAliasCreateBinding.inflate(inflater)

        binding.toolbar.setNavigationOnClickListener { dismissKeyboardAndNavigateUp() }

        val apiKey = SLSharedPreferences.getApiKey(requireContext()) ?: throw IllegalStateException("API key is null")

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
                        context?.toastLongly("You can not create more alias. Please upgrade to premium.")
                        findNavController().navigateUp()
                    }
                }
            }
        }

        return binding.root
    }

    private fun dismissKeyboardAndNavigateUp() {
        activity?.dismissKeyboard()
        findNavController().navigateUp()
    }

    private fun updateAliasListViewModelAndNavigateUp() {
        findNavController().navigateUp()
    }

    private fun setUpSuffixesSpinner(suffixes: List<String>) {
        binding.suffixesSpinner.adapter = AliasCreateSpinnerAdapter(requireContext(), suffixes)
        binding.suffixesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        dismissKeyboardAndNavigateUp()
    }
}
