package io.simplelogin.android.module.about

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.databinding.FragmentAboutBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.getVersionName

class AboutFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentAboutBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { showLeftMenu() }

        binding.appVersionTextView.text = "SimpleLogin v${context?.getVersionName()}"
        binding.howTextView.setOnClickListener { findNavController().navigate(AboutFragmentDirections.actionAboutFragmentToHowItWorksFragment()) }
        return binding.root
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() = showLeftMenu()
}