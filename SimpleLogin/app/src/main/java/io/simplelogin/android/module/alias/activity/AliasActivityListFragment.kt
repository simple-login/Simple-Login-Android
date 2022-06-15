package io.simplelogin.android.module.alias.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.simplelogin.android.databinding.DialogViewEditTextBinding
import io.simplelogin.android.databinding.FragmentAliasActivityBinding
import io.simplelogin.android.module.alias.AliasListViewModel
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.LoadingFooterAdapter
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.*
import io.simplelogin.android.utils.model.AliasActivity

class AliasActivityListFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentAliasActivityBinding
    private val aliasListViewModel: AliasListViewModel by activityViewModels()
    private lateinit var viewModel: AliasActivityListViewModel
    private lateinit var headerAdapter: AliasActivityListHeaderAdapter
    private lateinit var activityAdapter: AliasActivityListAdapter
    private val footerAdapter = LoadingFooterAdapter()
    private lateinit var linearSmoothScroller: LinearSmoothScroller

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAliasActivityBinding.inflate(inflater)

        setUpViewModel()

        binding.toolbar.setNavigationOnClickListener { updateAliasListViewModelAndNavigateUp() }
        binding.toolbarTitleText.text = viewModel.alias.email
        binding.toolbarTitleText.isSelected = true // to trigger marquee animation

        binding.scrollToTopButton.hide()
        binding.scrollToTopButton.setOnClickListener {
            linearSmoothScroller.targetPosition = 0
            binding.recyclerView.layoutManager?.startSmoothScroll(linearSmoothScroller)
        }

        setUpRecyclerView()
        setLoading(false)

        return binding.root
    }

    private fun setLoading(loading: Boolean) {
        binding.rootConstraintLayout.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showLoadingFooter(showing: Boolean) {
        footerAdapter.isLoading = showing
        footerAdapter.notifyDataSetChanged()
    }

    private fun setUpViewModel() {
        val alias = AliasActivityListFragmentArgs.fromBundle(requireArguments()).alias

        val tempViewModel: AliasActivityListViewModel by viewModels {
            context?.let {
                AliasActivityListViewModelFactory(it, alias)
            } ?: throw IllegalStateException("Context is null")
        }
        viewModel = tempViewModel
        viewModel.fetchActivities()
        viewModel.eventHaveNewActivities.observe(viewLifecycleOwner) { haveNewActivities ->
            activity?.runOnUiThread {
                showLoadingFooter(false)

                if (haveNewActivities) {
                    activityAdapter.submitList(viewModel.activities.toMutableList())
                    viewModel.onHandleHaveNewActivitiesComplete()
                }

                if (binding.swipeRefreshLayout.isRefreshing) {
                    context?.toastUpToDate()
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                setLoading(false)
                showLoadingFooter(true)
                context?.toastError(error)
                viewModel.onHandleErrorComplete()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        viewModel.eventUpdateMetadata.observe(viewLifecycleOwner) { metadataUpdated ->
            if (metadataUpdated) {
                setLoading(false)
                headerAdapter.notifyDataSetChanged()
                viewModel.onHandleUpdateMetadataComplete()
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

        headerAdapter = AliasActivityListHeaderAdapter(
            viewModel,
            object : AliasActivityListHeaderAdapter.ClickListener {
                override fun editMailboxesButtonClicked() {
                    fetchMailboxesAndShowAlert()
                }

                override fun editNameButtonClicked() {
                    val dialogTextViewBinding = DialogViewEditTextBinding.inflate(layoutInflater)
                    dialogTextViewBinding.editText.hint = "Ex: Jane Doe"
                    dialogTextViewBinding.editText.setText(viewModel.alias.name)
                    val title = when (viewModel.alias.name) {
                        null -> "Add name for alias"
                        else -> "Edit name for alias"
                    }

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(title)
                        .setMessage(viewModel.alias.email)
                        .setView(dialogTextViewBinding.root)
                        .setNeutralButton("Cancel", null)
                        .setPositiveButton("Save") { _, _ ->
                            viewModel.updateName(dialogTextViewBinding.editText.text.toString())
                        }
                        .show()
                }

                override fun editNoteButtonClicked() {
                    val dialogTextViewBinding = DialogViewEditTextBinding.inflate(layoutInflater)
                    dialogTextViewBinding.editText.hint =
                        "Ex: For tech newsletters, online shopping..."
                    dialogTextViewBinding.editText.setText(viewModel.alias.note)
                    val title = when (viewModel.alias.note) {
                        null -> "Add note for alias"
                        else -> "Edit note for alias"
                    }

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(title)
                        .setMessage(viewModel.alias.email)
                        .setView(dialogTextViewBinding.root)
                        .setNeutralButton("Cancel", null)
                        .setPositiveButton("Save") { _, _ ->
                            viewModel.updateNote(dialogTextViewBinding.editText.text.toString())
                        }
                        .show()
                }
            })

        activityAdapter = AliasActivityListAdapter(object : AliasActivityListAdapter.ClickListener {
            override fun onClick(aliasActivity: AliasActivity) {
                activity?.alertReversableOptions(aliasActivity, viewModel.alias)
            }
        })

        binding.recyclerView.adapter = ConcatAdapter(headerAdapter, activityAdapter, footerAdapter)
        val linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = linearLayoutManager

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val isLastItem =
                    linearLayoutManager.findLastCompletelyVisibleItemPosition() == viewModel.activities.size
                if (isLastItem && viewModel.moreToLoad) {
                    showLoadingFooter(true)
                    viewModel.fetchActivities()
                }

                if (dy >= 0) {
                    binding.scrollToTopButton.hide()
                } else if (linearLayoutManager.findLastCompletelyVisibleItemPosition() > 20) {
                    binding.scrollToTopButton.show()
                }
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshAlias()
            viewModel.refreshActivities()
        }
    }

    private fun fetchMailboxesAndShowAlert() {
        setLoading(true)
        SLApiService.fetchMailboxes(viewModel.apiKey) { result ->
            activity?.runOnUiThread {
                setLoading(false)

                result.onFailure(requireContext()::toastThrowable)

                result.onSuccess { mailboxes ->
                    activity?.showSelectMailboxesAlert(
                        mailboxes,
                        viewModel.alias.mailboxes
                    ) { checkedMailboxes ->
                        setLoading(true)
                        viewModel.updateMailboxes(checkedMailboxes)
                    }
                }
            }
        }
    }

    private fun refreshAlias() {
        setLoading(true)
        SLApiService.getAlias(viewModel.apiKey, viewModel.alias.id) { result ->
            activity?.runOnUiThread {
                setLoading(false)

                result.onSuccess { alias ->
                    viewModel.alias = alias
                    headerAdapter.notifyDataSetChanged()
                }

                result.onFailure(requireContext()::toastThrowable)
            }
        }
    }

    private fun updateAliasListViewModelAndNavigateUp() {
        aliasListViewModel.updateAlias(viewModel.alias)
        findNavController().navigateUp()
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        updateAliasListViewModelAndNavigateUp()
    }
}
