package io.simplelogin.android.module.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.databinding.FragmentPremiumBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.baseclass.BaseFragment

class PremiumFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentPremiumBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPremiumBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        return binding.root
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        findNavController().navigateUp()
    }
}