package io.simplelogin.android.module.alias.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.databinding.FragmentAliasSearchBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.dismissKeyboard
import io.simplelogin.android.utils.extension.showKeyboard

class AliasSearchFragment : BaseFragment(), HomeActivity.OnBackPressed {
    lateinit var binding: FragmentAliasSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAliasSearchBinding.inflate(inflater)
        binding.closeButton.setOnClickListener {
            activity?.dismissKeyboard()
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.searchEditText.requestFocus()
        activity?.showKeyboard()
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        activity?.dismissKeyboard()
        findNavController().navigateUp()
    }
}