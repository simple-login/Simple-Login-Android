package io.simplelogin.android.module.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.simplelogin.android.databinding.FragmentAboutBinding
import io.simplelogin.android.utils.baseclass.BaseFragment

class AboutFragment : BaseFragment() {
    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { showLeftMenu() }
        return binding.root
    }
}