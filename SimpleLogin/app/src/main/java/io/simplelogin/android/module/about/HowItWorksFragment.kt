package io.simplelogin.android.module.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.databinding.FragmentHowItWorksBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.baseclass.BaseFragment

class HowItWorksFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentHowItWorksBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHowItWorksBinding.inflate(inflater)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        return binding.root
    }

    override fun onBackPressed() {
        findNavController().navigateUp()
    }
}
