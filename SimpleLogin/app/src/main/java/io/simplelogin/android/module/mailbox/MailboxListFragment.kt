package io.simplelogin.android.module.mailbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.simplelogin.android.R
import io.simplelogin.android.databinding.FragmentMailboxListBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.*

class MailboxListFragment : BaseFragment(), HomeActivity.OnBackPressed,
    Toolbar.OnMenuItemClickListener {
    private lateinit var binding: FragmentMailboxListBinding
    private lateinit var viewModel: MailboxListViewModel
    private lateinit var adapter: MailboxListAdapter
    private lateinit var howToUseMailboxBehavior: BottomSheetBehavior<View>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setUpBinding()
        setUpHowToUseMailboxBottomSheet()
        setUpViewModel()
        setUpRecyclerView()

        setLoading(true)
        viewModel.fetchMailboxes()

        return binding.root
    }

    private fun setLoading(loading: Boolean) {
        binding.rootConstraintLayout.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setUpBinding() {
        binding = FragmentMailboxListBinding.inflate(layoutInflater)
        binding.toolbar.setNavigationOnClickListener { showLeftMenu()  }
        binding.toolbar.setOnMenuItemClickListener(this)
    }

    private fun setUpHowToUseMailboxBottomSheet() {
        binding.howToUseMailboxBottomSheet.root.layoutParams.height =
            requireActivity().getScreenHeight() * 90 / 100

        howToUseMailboxBehavior =
            BottomSheetBehavior.from(binding.howToUseMailboxBottomSheet.root)
        howToUseMailboxBehavior.hide()
        binding.howToUseMailboxBottomSheet.closeButton.setOnClickListener { howToUseMailboxBehavior.hide() }

        howToUseMailboxBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.dimView.alpha = slideOffset * 60 / 100
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> binding.dimView.visibility = View.GONE

                    else -> {
                        binding.dimView.visibility = View.VISIBLE
                        binding.dimView.setOnTouchListener { _, _ ->
                            // Must return true here to intercept touch event
                            // if not the event is passed to next listener which cause the whole root is clickable
                            true
                        }
                    }
                }
            }
        })
    }

    private fun setUpViewModel() {
        viewModel = MailboxListViewModel(requireContext())

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                setLoading(false)
                context?.toastError(error)
                viewModel.onHandleErrorComplete()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        })

        viewModel.eventUpdateMailboxes.observe(viewLifecycleOwner, Observer { haveNewMailboxes ->
            activity?.runOnUiThread {
                setLoading(false)

                if (haveNewMailboxes) {
                    adapter.submitList(viewModel.mailboxes)
                }

                if (binding.swipeRefreshLayout.isRefreshing) {
                    context?.toastUpToDate()
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    private fun setUpRecyclerView() {
        adapter = MailboxListAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.fetchMailboxes() }
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        when {
            howToUseMailboxBehavior.isExpanded() -> howToUseMailboxBehavior.hide()
            else -> findNavController().navigateUp()
        }
    }

    // Toolbar.OnMenuItemClickListener
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.addMenuItem -> {
                // add mailbox
            }

            R.id.howToMenuItem -> howToUseMailboxBehavior.expand()
        }

        return true
    }
}