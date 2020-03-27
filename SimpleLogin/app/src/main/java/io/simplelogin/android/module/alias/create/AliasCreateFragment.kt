package io.simplelogin.android.module.alias.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.databinding.FragmentAliasCreateBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.baseclass.BaseFragment

class AliasCreateFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentAliasCreateBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAliasCreateBinding.inflate(inflater)

        binding.toolbar.setNavigationOnClickListener { updateAliasListViewModelAndNavigateUp() }

        return binding.root
    }

    private fun updateAliasListViewModelAndNavigateUp() {
        findNavController().navigateUp()
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        updateAliasListViewModelAndNavigateUp()
    }
}