package io.simplelogin.android.module.alias

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import io.simplelogin.android.R
import io.simplelogin.android.databinding.DialogViewEditTextBinding
import io.simplelogin.android.databinding.FragmentAliasListBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SwipeToDeleteCallback
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.enums.AliasFilterMode
import io.simplelogin.android.utils.enums.RandomMode
import io.simplelogin.android.utils.extension.*
import io.simplelogin.android.utils.model.Alias
import java.lang.Exception

class AliasListFragment : BaseFragment(), Toolbar.OnMenuItemClickListener,
    TabLayout.OnTabSelectedListener, HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentAliasListBinding
    private val viewModel: AliasListViewModel by activityViewModels()
    private lateinit var adapter: AliasListAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var lastToast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAliasListBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { showLeftMenu() }
        binding.toolbar.setOnMenuItemClickListener(this)
        binding.tabLayout.addOnTabSelectedListener(this)

        setUpViewModel()
        // Reset tab selection state on configuration changed
        binding.tabLayout.getTabAt(viewModel.aliasFilterMode.position)?.select()

        setUpRecyclerView()
        setLoading(false)
        viewModel.fetchAliases()

        firebaseAnalytics.logEvent("open_alias_list_fragment", null)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // On configuration change, trigger a recyclerView refresh by calling filter function
        if (adapter.itemCount == 0) {
            viewModel.filterAliases()
        }

        if (viewModel.needsShowPricing) {
            // Delay here waiting for AliasCreateFragment finish navigateUp()
            Handler().postDelayed({
                findNavController().navigate(
                    AliasListFragmentDirections.actionAliasListFragmentToWebViewFragment(
                        "https://simplelogin.io/pricing"
                    )
                )
            }, 100)

            viewModel.onHandleShowPricingComplete()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.rootConstraintLayout.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setUpViewModel() {
        viewModel.eventUpdateAliases.observe(
            viewLifecycleOwner,
            Observer { updatedAliases ->
                activity?.runOnUiThread {
                    if (updatedAliases) {
                        setLoading(false)
                        lastToast?.cancel()
                        // filteredAliases.toMutableList() to make the recyclerView updates itself
                        // it not, we have to call adapter.notifyDataSetChanged() which breaks the animation. ListAdapter bug?
                        adapter.submitList(viewModel.filteredAliases.toMutableList())

                        viewModel.onEventUpdateAliasesComplete()

                        if (binding.swipeRefreshLayout.isRefreshing) {
                            binding.swipeRefreshLayout.isRefreshing = false
                            context?.toastUpToDate()
                        }
                    }
                }
            })

        viewModel.toggledAliasIndex.observe(viewLifecycleOwner, Observer { toggledAliasIndex ->
            if (toggledAliasIndex != null) {
                activity?.runOnUiThread {
                    setLoading(false)
                    adapter.notifyItemChanged(toggledAliasIndex)
                    viewModel.onHandleToggleAliasComplete()
                }
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                context?.toastError(error)
                viewModel.onHandleErrorComplete()
                binding.swipeRefreshLayout.isRefreshing = false
                firebaseAnalytics.logEvent("alias_list_error", error.toBundle())
            }
        })
    }

    private fun setUpRecyclerView() {
        adapter = AliasListAdapter(object : AliasListAdapter.ClickListener {
            val context = getContext() ?: throw Exception("Context is null")

            override fun onClick(alias: Alias) {
                findNavController().navigate(
                    AliasListFragmentDirections.actionAliasListFragmentToAliasActivityListFragment(
                        alias
                    )
                )
                firebaseAnalytics.logEvent("alias_list_view_activities", null)
            }

            override fun onSwitch(alias: Alias, position: Int) {
                setLoading(true)
                viewModel.toggleAlias(alias, position, firebaseAnalytics)
            }

            override fun onCopy(alias: Alias) {
                val email = alias.email
                copyToClipboard(email, email)
                context.toastShortly("Copied \"$email\"")

                firebaseAnalytics.logEvent("alias_list_copy", null)
            }

            override fun onSendEmail(alias: Alias) {
                findNavController().navigate(
                    AliasListFragmentDirections.actionAliasFragmentToContactListFragment(
                        alias
                    )
                )
                firebaseAnalytics.logEvent("alias_list_view_contacts", null)
            }
        })
        binding.recyclerView.adapter = adapter
        linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = linearLayoutManager

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if ((linearLayoutManager.findLastCompletelyVisibleItemPosition() == viewModel.filteredAliases.size - 1)
                    && viewModel.moreAliasesToLoad
                ) {
                    setLoading(true)
                    lastToast = context?.toastShortly("Loading more...")
                    viewModel.fetchAliases()
                    firebaseAnalytics.logEvent("alias_list_fetch_more", null)
                }
            }
        })

        // Add swipe recognizer to recyclerView
        val itemTouchHelper = ItemTouchHelper(object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val alias = viewModel.filteredAliases[viewHolder.adapterPosition]
                MaterialAlertDialogBuilder(context)
                    .setTitle("Delete \"${alias.email}\"?")
                    .setMessage("\uD83D\uDED1 People/apps who used to contact you via this alias cannot reach you any more. This operation is irreversible. Please confirm.")
                    .setNegativeButton("Delete") { _, _ ->
                        setLoading(true)
                        viewModel.deleteAlias(alias, firebaseAnalytics)
                    }
                    .setNeutralButton("Cancel", null)
                    .setOnDismissListener {
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                    }
                    .show()
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        // Refresh capacity
        binding.swipeRefreshLayout.setOnRefreshListener {
            firebaseAnalytics.logEvent("alias_list_refresh", null)
            viewModel.refreshAliases()
        }
    }

    private fun showSelectRandomModeAlert() {
        MaterialAlertDialogBuilder(context, R.style.SlAlertDialogTheme)
            .setTitle("Randomly create an alias")
            .setItems(
                arrayOf("By random words", "By UUID")
            ) { _, itemIndex ->
                val randomMode = when (itemIndex) {
                    0 -> RandomMode.WORD
                    else -> RandomMode.UUID
                }
                showAddNoteAlert(randomMode)
            }
            .show()
    }

    private fun showAddNoteAlert(randomMode: RandomMode) {
        val dialogTextViewBinding = DialogViewEditTextBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(context)
            .setTitle("Add some note for this alias")
            .setMessage("This is optional and can be modified ar anytime later")
            .setView(dialogTextViewBinding.root)
            .setNeutralButton("Cancel", null)
            .setPositiveButton("Create") { _, _ ->
                setLoading(true)
                SLApiService.randomAlias(
                    viewModel.apiKey,
                    randomMode,
                    dialogTextViewBinding.editText.text.toString()
                ) { alias, error ->
                    activity?.runOnUiThread {
                        setLoading(false)
                        if (error != null) {
                            context?.toastError(error)

                            when (randomMode) {
                                RandomMode.UUID -> firebaseAnalytics.logEvent("alias_random_by_uuid_error", error.toBundle())
                                RandomMode.WORD -> firebaseAnalytics.logEvent("alias_random_by_word_error", error.toBundle())
                            }

                        } else if (alias != null) {
                            viewModel.addAlias(alias)
                            viewModel.filterAliases()
                            binding.recyclerView.smoothScrollToPosition(0)
                            context?.toastShortly("Created \"${alias.email}\"")

                            when (randomMode) {
                                RandomMode.UUID -> firebaseAnalytics.logEvent("alias_random_by_uuid_success", null)
                                RandomMode.WORD -> firebaseAnalytics.logEvent("alias_random_by_word_success", null)
                            }
                        }
                    }
                }
            }
            .show()

        dialogTextViewBinding.editText.requestFocus()
    }

    // Toolbar.OnMenuItemClickListener
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.searchMenuItem -> findNavController().navigate(AliasListFragmentDirections.actionAliasListFragmentToAliasSearchFragment())
            R.id.randomMenuItem -> showSelectRandomModeAlert()
            R.id.addMenuItem -> findNavController().navigate(AliasListFragmentDirections.actionAliasListFragmentToAliasCreateFragment())
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

        firebaseAnalytics.logEvent("alias_list_change_filter_mode", null)
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() = showLeftMenu()
}