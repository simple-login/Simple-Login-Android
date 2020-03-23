package io.simplelogin.android.module.alias.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.databinding.FragmentAliasActivityBinding
import io.simplelogin.android.module.alias.contact.ContactListFragmentArgs
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.model.Alias

class AliasActivityListFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentAliasActivityBinding
    private lateinit var alias: Alias

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

        return binding.root
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        findNavController().navigateUp()
    }
}