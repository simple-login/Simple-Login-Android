package io.simplelogin.android.module.alias.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.databinding.FragmentContactListBinding
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.model.Alias

class ContactListFragment : BaseFragment() {
    private lateinit var binding: FragmentContactListBinding
    private lateinit var alias: Alias

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactListBinding.inflate(layoutInflater)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        alias = ContactListFragmentArgs.fromBundle(requireArguments()).alias

        binding.emailTextField.text = alias.email
        binding.emailTextField.isSelected = true // to trigger marquee animation

        return binding.root
    }
}