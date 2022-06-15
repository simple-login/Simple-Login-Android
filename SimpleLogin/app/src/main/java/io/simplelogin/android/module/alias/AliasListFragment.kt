package io.simplelogin.android.module.alias

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import io.simplelogin.android.R
import io.simplelogin.android.databinding.FragmentAliasListBinding
import io.simplelogin.android.module.alias.search.AliasSearchMode
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.LoadingFooterAdapter
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SwipeHelper
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.enums.AliasFilterMode
import io.simplelogin.android.utils.enums.RandomMode
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.extension.*
import io.simplelogin.android.utils.model.Alias

class AliasListFragment : BaseFragment(), Toolbar.OnMenuItemClickListener,
    TabLayout.OnTabSelectedListener, HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentAliasListBinding
    private val viewModel: AliasListViewModel by activityViewModels()
    private lateinit var aliasListAdapter: AliasListAdapter
    private val footerAdapter = LoadingFooterAdapter()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var linearSmoothScroller: LinearSmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAliasListBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { showLeftMenu() }
        binding.toolbar.setOnMenuItemClickListener(this)
        binding.tabLayout.addOnTabSelectedListener(this)

        setUpViewModel()
        // Reset tab selection state on configuration changed
        binding.tabLayout.getTabAt(viewModel.aliasFilterMode.position)?.select()

        binding.scrollToTopButton.hide()
        binding.scrollToTopButton.setOnClickListener {
            linearSmoothScroller.targetPosition = 0
            binding.recyclerView.layoutManager?.startSmoothScroll(linearSmoothScroller)
        }

        setUpRecyclerView()
        setLoading(false)
        // Do not fetch more aliases on configuration changed
        if (viewModel.filteredAliases.isEmpty()) {
            viewModel.fetchAliases()
        }
        activity?.intent?.let { viewModel.getMailToEmail(it) }

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        viewModel.setLastScrollingPosition(linearLayoutManager.findFirstVisibleItemPosition())
    }

    @Suppress("MagicNumber")
    override fun onResume() {
        super.onResume()
        // On configuration change, trigger a recyclerView refresh by calling filter function
        if (aliasListAdapter.itemCount == 0) {
            aliasListAdapter.submitList(viewModel.filteredAliases.toMutableList())
            binding.recyclerView.scrollToPosition(viewModel.getLastScrollingPosition())
        }

        if (viewModel.needsShowPricing) {
            // Delay here waiting for AliasCreateFragment finish navigateUp()
            Handler(Looper.getMainLooper()).postDelayed({
                navigateToPricingPage()
            }, 100)

            viewModel.onHandleShowPricingComplete()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.rootConstraintLayout.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showLoadingFooter(showing: Boolean) {
        footerAdapter.isLoading = showing
        footerAdapter.notifyDataSetChanged()
    }

    @Suppress("MaxLineLength")
    private fun setUpViewModel() {
        viewModel.eventUpdateAliases.observe(
            viewLifecycleOwner
        ) { updatedAliases ->
            activity?.runOnUiThread {
                setLoading(false)
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

        viewModel.toggledAliasIndex.observe(viewLifecycleOwner) { toggledAliasIndex ->
            if (toggledAliasIndex != null) {
                activity?.runOnUiThread {
                    setLoading(false)
                    aliasListAdapter.notifyItemChanged(toggledAliasIndex)
                    viewModel.onHandleToggleAliasComplete()
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

        viewModel.shouldActionOnMailToEmail.observe(viewLifecycleOwner) { shouldAction ->
            val mailToEmail = viewModel.mailToEmail ?: return@observe
            if (shouldAction) {
                MaterialAlertDialogBuilder(requireContext(), R.style.SlAlertDialogTheme)
                    .setTitle("Email to \"$mailToEmail\"")
                    .setItems(
                        arrayOf("Pick an alias", "Random an alias", "Create an alias")
                    ) { _, itemIndex ->
                        when (itemIndex) {
                            0 -> findNavController().navigate(AliasListFragmentDirections.actionAliasListFragmentToAliasPickerFragment())
                            1 -> randomAlias(null, true)
                            2 -> findNavController().navigate(
                                AliasListFragmentDirections.actionAliasListFragmentToAliasCreateFragment(
                                    true
                                )
                            )
                        }
                    }
                    .show()
                viewModel.onActionOnMailToEmailComplete()
            }
        }

        viewModel.mailFromAlias.observe(viewLifecycleOwner) { mailFromAlias ->
            if (mailFromAlias != null) {
                viewModel.createContact(mailFromAlias)
            }
        }

        viewModel.createdContact.observe(viewLifecycleOwner) { createdContact ->
            activity?.runOnUiThread {
                if (createdContact != null) {
                    activity?.alertReversableOptions(createdContact, viewModel.mailFromAlias.value)
                    viewModel.onHandleCreatedContactComplete()
                }
            }
        }
    }

    @Suppress("MagicNumber")
    private fun setUpRecyclerView() {
        linearSmoothScroller = object : LinearSmoothScroller(requireContext()) {
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                // MILLISECONDS_PER_INCH / displayMetrics.densityDpi
                return 5f / (displayMetrics?.densityDpi ?: 1)
            }

            override fun onStop() {
                binding.scrollToTopButton.hide()
                super.onStop()
            }
        }

        aliasListAdapter = AliasListAdapter(object : AliasListAdapter.ClickListener {
            val context = getContext() ?: throw IllegalStateException("Context is null")

            override fun onClick(alias: Alias) {
                findNavController().navigate(
                    AliasListFragmentDirections.actionAliasListFragmentToAliasActivityListFragment(
                        alias
                    )
                )
            }

            override fun onSwitch(alias: Alias, position: Int) {
                setLoading(true)
                viewModel.toggleAlias(alias, position)
            }

            override fun onCopy(alias: Alias) {
                val email = alias.email
                copyToClipboard(email, email)
                context.toastShortly("Copied \"$email\"")
            }

            override fun onSendEmail(alias: Alias) {
                findNavController().navigate(
                    AliasListFragmentDirections.actionAliasFragmentToContactListFragment(
                        alias
                    )
                )
            }
        })
        binding.recyclerView.adapter = ConcatAdapter(aliasListAdapter, footerAdapter)
        linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = linearLayoutManager

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val isPenultimateItem =
                    linearLayoutManager.findLastCompletelyVisibleItemPosition() == viewModel.filteredAliases.size - 1
                if (isPenultimateItem && viewModel.moreAliasesToLoad) {
                    showLoadingFooter(true)
                    viewModel.fetchAliases()
                }

                if (dy >= 0) {
                    binding.scrollToTopButton.hide()
                } else if (linearLayoutManager.findLastCompletelyVisibleItemPosition() > 10) {
                    binding.scrollToTopButton.show()
                }
            }
        })

        // Add swipe recognizer to recyclerView
        val itemTouchHelper = ItemTouchHelper(object : SwipeHelper(binding.recyclerView) {
            override fun instantiateUnderlayButton(position: Int): List<UnderlayButton> {
                return listOf(
                    UnderlayButton(
                        requireContext(),
                        "Delete",
                        UnderlayButton.DEFAULT_TEXT_SIZE,
                        android.R.color.holo_red_light,
                        object : UnderlayButtonClickListener {
                            override fun onClick() {
                                val alias = viewModel.filteredAliases[position]
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Delete \"${alias.email}\"?")
                                    .setMessage(R.string.warning_before_deleting_alias)
                                    .setNegativeButton("Delete") { _, _ ->
                                        setLoading(true)
                                        viewModel.deleteAlias(alias)
                                    }
                                    .setNeutralButton("Cancel", null)
                                    .show()
                            }
                        })
                )
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        // Refresh capacity
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refreshAliases() }
    }

    private fun showSelectRandomModeAlert() {
        MaterialAlertDialogBuilder(requireContext(), R.style.SlAlertDialogTheme)
            .setTitle("Randomly create an alias")
            .setItems(
                arrayOf("By random words", "By UUID")
            ) { _, itemIndex ->
                val randomMode = when (itemIndex) {
                    0 -> RandomMode.WORD
                    else -> RandomMode.UUID
                }
                randomAlias(randomMode)
            }
            .show()
    }

    private fun randomAlias(randomMode: RandomMode?, isMailFromAlias: Boolean = false) {
        setLoading(true)
        SLApiService.randomAlias(viewModel.apiKey, randomMode, "") { result ->
            activity?.runOnUiThread {
                setLoading(false)

                result.onSuccess { alias ->
                    viewModel.addAlias(alias)
                    viewModel.filterAliases()
                    binding.recyclerView.smoothScrollToPosition(0)
                    if (isMailFromAlias) {
                        viewModel.setMailFromAlias(alias)
                    } else {
                        context?.toastShortly("Created \"${alias.email}\"")
                    }
                }

                result.onFailure { error ->
                    when (error) {
                        is SLError.CanNotCreateMoreAlias -> alertCanNotCreateMoreAlias()
                        else -> context?.toastThrowable(error)
                    }
                }
            }
        }
    }

    private fun alertCanNotCreateMoreAlias() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Can not create more alias")
            .setMessage("Go premium for unlimited aliases and more.")
            .setPositiveButton("See pricing", null)
            .setOnDismissListener {
                navigateToPricingPage()
            }
            .show()
    }

    private fun navigateToPricingPage() {
        findNavController().navigate(
            AliasListFragmentDirections.actionAliasListFragmentToWebViewFragment(
                "https://simplelogin.io/pricing"
            )
        )
    }

    // Toolbar.OnMenuItemClickListener
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.searchMenuItem ->
                findNavController().navigate(AliasListFragmentDirections.actionAliasListFragmentToAliasSearchFragment(AliasSearchMode.DEFAULT))
            R.id.randomMenuItem -> showSelectRandomModeAlert()
            R.id.addMenuItem ->
                findNavController().navigate(
                    AliasListFragmentDirections.actionAliasListFragmentToAliasCreateFragment(
                        false
                    )
                )
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

    // HomeActivity.OnBackPressed
    override fun onBackPressed() = showLeftMenu()
}
