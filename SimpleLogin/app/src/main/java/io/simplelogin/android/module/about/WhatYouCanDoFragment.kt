package io.simplelogin.android.module.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.databinding.FragmentWhatYouCanDoBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.baseclass.BaseFragment

class WhatYouCanDoFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentWhatYouCanDoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWhatYouCanDoBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        return binding.root
    }

    override fun onBackPressed() {
        findNavController().navigateUp()
    }
}
