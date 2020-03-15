package io.simplelogin.android.module.alias

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.simplelogin.android.databinding.FragmentAliasBinding
import io.simplelogin.android.utils.baseclass.BaseFragment

class AliasFragment : BaseFragment() {
    lateinit var binding: FragmentAliasBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAliasBinding.inflate(inflater)

        binding.toolbar.setNavigationOnClickListener { showLeftMenu() }

        return binding.root
    }
}