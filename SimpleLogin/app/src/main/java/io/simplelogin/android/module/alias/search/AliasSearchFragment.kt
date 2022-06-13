package io.simplelogin.android.module.alias.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.simplelogin.android.R
import io.simplelogin.android.databinding.FragmentAliasSearchBinding
import io.simplelogin.android.module.alias.AliasListAdapter
import io.simplelogin.android.module.alias.AliasListViewModel
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.SwipeHelper
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.*
import io.simplelogin.android.utils.model.Alias

enum class AliasSearchMode {
    DEFAULT, CONTACT_CREATION
}

class AliasSearchFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentAliasSearchBinding
    private val aliasListViewModel: AliasListViewModel by activityViewModels()
    private lateinit var viewModel: AliasSearchViewModel
    private lateinit var adapter: AliasSearchAdapter
    private val searchMode: AliasSearchMode by lazy {
        AliasSearchFragmentArgs.fromBundle(requireArguments()).searchMode
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAliasSearchBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        /*binding.backImageView.setOnClickListener {
            activity?.dismissKeyboard()
            updateAliasListViewModelAndNavigateUp()
        }*/

        setLoading(false)
        binding.messageTextView.visibility = View.GONE

        setUpSearchTextInputLayout()
        setUpViewModel()
        setUpRecyclerView()

        return binding.root
    }

    override fun onResume() {
        // Animate slide search bar
        super.onResume()
        /*val slideInFromLeftAnimator =
            AnimatorInflater.loadAnimator(context, R.animator.slide_in_from_left)
        slideInFromLeftAnimator.setTarget(binding.toolbarRootRelativeLayout)
        slideInFromLeftAnimator.start()*/

        // On configuration change
        if (viewModel.aliases.isEmpty()) {
            // Show keyboard
            binding.searchTextInputLayout.editText?.requestFocus()
            activity?.showKeyboard()
        } else {
            // Bind last search term back into editText
            binding.searchTextInputLayout.editText?.setText(viewModel.term)
            // Reload recyclerView
            viewModel.forceUpdateResults()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpSearchTextInputLayout() {
        binding.searchTextInputLayout.editText?.setOnKeyListener { _, keyCode, event ->
            // Must check event.action == KeyEvent.ACTION_DOWN
            // because a key is called 2 times: once for action down & once for action up
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                val term = binding.searchTextInputLayout.editText?.text.toString()
                if (term.count() < 2) {
                    context?.toastShortly("Minimum 2 characters required")
                } else {
                    binding.messageTextView.text = "No result for \"$term\""
                    setLoading(true)
                    activity?.dismissKeyboard()
                    viewModel.search(term)
                }
            }
            // False means the listener doesn't consume the event
            false
        }
    }

    private fun setUpViewModel() {
        val tempViewModel: AliasSearchViewModel by viewModels {
            context?.let {
                AliasSearchViewModelFactory(it)
            } ?: throw IllegalStateException("Context is null")
        }
        viewModel = tempViewModel

        viewModel.eventUpdateResults.observe(viewLifecycleOwner) { updatedResults ->
            if (updatedResults) {
                activity?.runOnUiThread {
                    setLoading(false)

                    if (viewModel.aliases.isEmpty()) {
                        binding.messageTextView.visibility = View.VISIBLE
                    } else {
                        binding.messageTextView.visibility = View.GONE
                    }

                    adapter.submitList(viewModel.aliases.toMutableList())
                    viewModel.onHandleUpdateResultsComplete()
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                activity?.run {
                    toastError(error)
                    viewModel.onHandleErrorComplete()
                }
            }
        }

        viewModel.toggledAliasIndex.observe(viewLifecycleOwner) { toggledAliasIndex ->
            if (toggledAliasIndex != null) {
                activity?.runOnUiThread {
                    setLoading(false)
                    adapter.notifyItemChanged(toggledAliasIndex)
                    viewModel.onHandleToggleAliasComplete()
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        adapter = AliasSearchAdapter(searchMode, object : AliasListAdapter.ClickListener {
            override fun onClick(alias: Alias) {
                when (searchMode) {
                    AliasSearchMode.DEFAULT ->
                        findNavController().navigate(
                            AliasSearchFragmentDirections.actionAliasSearchFragmentToAliasActivityListFragment(
                                alias
                            )
                        )
                    AliasSearchMode.CONTACT_CREATION -> {
                        aliasListViewModel.setMailFromAlias(alias)
                        findNavController().popBackStack(R.id.aliasListFragment, false)
                    }
                }
            }

            override fun onSwitch(alias: Alias, position: Int) {
                setLoading(true)
                viewModel.toggleAlias(alias, position)
            }

            override fun onCopy(alias: Alias) {
                val email = alias.email
                copyToClipboard(email, email)
                context?.toastShortly("Copied \"$email\"")
            }

            override fun onSendEmail(alias: Alias) {
                findNavController().navigate(
                    AliasSearchFragmentDirections.actionAliasSearchFragmentToContactListFragment(
                        alias
                    )
                )
            }
        })

        binding.recyclerView.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = linearLayoutManager

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                activity?.dismissKeyboard()
                val isPenultimateItem =
                    linearLayoutManager.findLastCompletelyVisibleItemPosition() == viewModel.aliases.size - 1
                if (isPenultimateItem && viewModel.moreToLoad) {
                    viewModel.search(null)
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
                                val alias = viewModel.aliases[position]
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
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun updateAliasListViewModelAndNavigateUp() {
        aliasListViewModel.updateToggledAndDeletedAliases(
            viewModel.toggledAliases,
            viewModel.deletedAliasIds
        )
        findNavController().navigateUp()
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        updateAliasListViewModelAndNavigateUp()
    }
}
