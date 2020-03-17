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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import io.simplelogin.android.R
import io.simplelogin.android.databinding.FragmentAliasListBinding
import io.simplelogin.android.module.home.HomeSharedViewModel
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.enums.AliasFilterMode
import io.simplelogin.android.utils.extension.toastError
import io.simplelogin.android.utils.model.Alias

class AliasListFragment : BaseFragment(), Toolbar.OnMenuItemClickListener,
    TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentAliasListBinding
    private val homeSharedViewModel: HomeSharedViewModel by activityViewModels()
    private lateinit var adapter: AliasListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAliasListBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { showLeftMenu() }
        binding.toolbar.setOnMenuItemClickListener(this)
        binding.tabLayout.addOnTabSelectedListener(this)

        // ViewModel
        homeSharedViewModel.fetchAliases()
        homeSharedViewModel.eventUpdateAliases.observe(
            viewLifecycleOwner,
            Observer { updatedAliases ->
                if (updatedAliases) {
                    activity?.runOnUiThread { adapter.setAliases(homeSharedViewModel.filteredAliases) }
                    homeSharedViewModel.onEventUpdateAliasesComplete()
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            })

        homeSharedViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                toastError(error)
                homeSharedViewModel.onHandleErrorComplete()
            }
        })

        // RecyclerView
        adapter = AliasListAdapter(object : AliasListAdapter.ClickListener {
            override fun onClick(alias: Alias) {

            }

            override fun onSwitch(alias: Alias, isChecked: Boolean) {

            }

            override fun onCopy(alias: Alias) {

            }

            override fun onSendEmail(alias: Alias) {

            }

            override fun onDelete(alias: Alias, position: Int) {

            }
        })
        binding.recyclerView.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = linearLayoutManager

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if ((linearLayoutManager.findLastCompletelyVisibleItemPosition() == homeSharedViewModel.filteredAliases.size - 1)
                    && homeSharedViewModel.moreAliasesToLoad
                ) {
                    homeSharedViewModel.fetchAliases()
                }
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener { homeSharedViewModel.refreshAliases() }

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
            0 -> homeSharedViewModel.filterAliases(AliasFilterMode.ALL)
            1 -> homeSharedViewModel.filterAliases(AliasFilterMode.ACTIVE)
            2 -> homeSharedViewModel.filterAliases(AliasFilterMode.INACTIVE)
        }
    }
}