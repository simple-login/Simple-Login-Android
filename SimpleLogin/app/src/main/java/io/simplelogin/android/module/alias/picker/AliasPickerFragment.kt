package io.simplelogin.android.module.alias.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import io.simplelogin.android.R
import io.simplelogin.android.databinding.FragmentAliasPickerBinding
import io.simplelogin.android.module.alias.AliasListViewModel
import io.simplelogin.android.module.alias.search.AliasSearchMode
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.LoadingFooterAdapter
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.enums.AliasFilterMode
import io.simplelogin.android.utils.extension.toastError
import io.simplelogin.android.utils.extension.toastUpToDate
import io.simplelogin.android.utils.model.Alias

class AliasPickerFragment : BaseFragment(), TabLayout.OnTabSelectedListener, Toolbar.OnMenuItemClickListener, HomeActivity.OnBackPressed {
    private val viewModel: AliasListViewModel by activityViewModels()
    private lateinit var binding: FragmentAliasPickerBinding
    private lateinit var aliasListAdapter: AliasPickerAdapter
    private val footerAdapter = LoadingFooterAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAliasPickerBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.tabLayout.addOnTabSelectedListener(this)
        binding.toolbar.setOnMenuItemClickListener(this)
        setUpViewModel()
        setUpRecyclerView()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // On configuration change, trigger a recyclerView refresh by calling filter function
        if (aliasListAdapter.itemCount == 0) {
            aliasListAdapter.submitList(viewModel.filteredAliases.toMutableList())
        }
    }

    private fun setUpViewModel() {
        viewModel.eventUpdateAliases.observe(
            viewLifecycleOwner
        ) { updatedAliases ->
            activity?.runOnUiThread {
                if (updatedAliases) {
                    showLoadingFooter(false)
                    // filteredAliases.toMutableList() to make the recyclerView updates itself
                    // it not, we have to call adapter.notifyDataSetChanged() which breaks the animation. ListAdapter bug?
                    aliasListAdapter.submitList(viewModel.filteredAliases.toMutableList())

                    viewModel.onEventUpdateAliasesComplete()

                    if (binding.swipeRefreshLayout.isRefreshing) {
                        binding.swipeRefreshLayout.isRefreshing = false
                        context?.toastUpToDate()
                    }
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                context?.toastError(error)
                viewModel.onHandleErrorComplete()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun setUpRecyclerView() {
        aliasListAdapter = AliasPickerAdapter(object : AliasPickerAdapter.ClickListener {
            override fun onClick(alias: Alias) {
                viewModel.setMailFromAlias(alias)
                findNavController().navigateUp()
            }
        })

        binding.recyclerView.adapter = ConcatAdapter(aliasListAdapter, footerAdapter)
        val linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refreshAliases() }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val isPenultimateItem =
                    linearLayoutManager.findLastCompletelyVisibleItemPosition() == viewModel.filteredAliases.size - 1
                if (isPenultimateItem  && viewModel.moreAliasesToLoad) {
                    showLoadingFooter(true)
                    viewModel.fetchAliases()
                }
            }
        })
    }

    private fun showLoadingFooter(showing: Boolean) {
        footerAdapter.isLoading = showing
        footerAdapter.notifyDataSetChanged()
    }

    // Toolbar.OnMenuItemClickListener
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.searchMenuItem ->
                findNavController().navigate(AliasPickerFragmentDirections.actionAliasPickerFragmentToAliasSearchFragment(AliasSearchMode.CONTACT_CREATION))
        }

        return true
    }

    // TabLayout.OnTabSelectedListener
    override fun onTabReselected(tab: TabLayout.Tab?) = Unit
    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
    override fun onTabSelected(tab: TabLayout.Tab?) {
        when (tab?.position) {
            0 -> viewModel.filterAliases(AliasFilterMode.ALL)
            1 -> viewModel.filterAliases(AliasFilterMode.ACTIVE)
            2 -> viewModel.filterAliases(AliasFilterMode.INACTIVE)
        }
    }

    override fun onBackPressed() {
        findNavController().navigateUp()
    }
}