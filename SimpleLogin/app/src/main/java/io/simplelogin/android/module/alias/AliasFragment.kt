package io.simplelogin.android.module.alias

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import io.simplelogin.android.R
import io.simplelogin.android.databinding.FragmentAliasBinding
import io.simplelogin.android.module.home.HomeSharedViewModel
import io.simplelogin.android.utils.baseclass.BaseFragment

class AliasFragment : BaseFragment(), Toolbar.OnMenuItemClickListener, TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentAliasBinding
    private val homeSharedViewModel: HomeSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAliasBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { showLeftMenu() }
        binding.toolbar.setOnMenuItemClickListener(this)
        binding.tabLayout.addOnTabSelectedListener(this)

        homeSharedViewModel.fetchAliases()

        homeSharedViewModel.aliases.observe(viewLifecycleOwner, Observer { aliases ->

        })

        return binding.root
    }

    // Toolbar.OnMenuItemClickListener
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.searchMenuItem -> {
                Log.d("menu", "search")
            }

            R.id.randomMenuItem -> {
                Log.d("menu", "shuffle")
            }

            R.id.addMenuItem -> {
                Log.d("menu", "add")
            }
        }

        return true
    }

    // TabLayout.OnTabSelectedListener
    override fun onTabReselected(tab: TabLayout.Tab?) = Unit
    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
    override fun onTabSelected(tab: TabLayout.Tab?) {
        when (tab?.position) {
            0 -> {
                // All
                Log.d("tab", "all")
            }

            1 -> {
                // All
                Log.d("tab", "active")
            }

            2 -> {
                // All
                Log.d("tab", "inactive")
            }
        }
    }
}