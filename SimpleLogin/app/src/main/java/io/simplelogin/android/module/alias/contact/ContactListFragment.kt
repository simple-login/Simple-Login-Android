package io.simplelogin.android.module.alias.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.databinding.FragmentContactListBinding
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.toastError
import io.simplelogin.android.utils.model.Alias

class ContactListFragment : BaseFragment() {
    private lateinit var binding: FragmentContactListBinding
    private lateinit var alias: Alias
    private lateinit var viewModel: ContactListViewModel
    private lateinit var adapter: ContactListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Binding
        binding = FragmentContactListBinding.inflate(layoutInflater)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        alias = ContactListFragmentArgs.fromBundle(requireArguments()).alias

        binding.emailTextField.text = alias.email
        binding.emailTextField.isSelected = true // to trigger marquee animation

        // ViewModel
        val tempViewModel: ContactListViewModel by viewModels {
            ContactListViewModelFactory(
                context ?: throw IllegalStateException("Context is null"), alias
            )
        }
        viewModel = tempViewModel
        viewModel.fetchContacts()
        viewModel.eventUpdateContacts.observe(viewLifecycleOwner, Observer { updatedContacts ->
            if (updatedContacts) {
                activity?.runOnUiThread {
                    adapter.submitList(viewModel.contacts)
                    viewModel.onEventUpdateContactsComplete()
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                toastError(error)
                viewModel.onHandleErrorComplete()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        })

        // RecyclerView
        adapter = ContactListAdapter()
        binding.recyclerView.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = linearLayoutManager

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if ((linearLayoutManager.findLastCompletelyVisibleItemPosition() == viewModel.contacts.size - 1) && viewModel.moreToLoad) {
                    viewModel.fetchContacts()
                }
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refreshContacts() }
        setLoading(false)
        return binding.root
    }

    private fun setLoading(loading: Boolean) {
        binding.rootConstraintLayout.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}