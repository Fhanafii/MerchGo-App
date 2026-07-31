package com.fhanafi.pitjarus.ui.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fhanafi.pitjarus.R
import com.fhanafi.pitjarus.databinding.FragmentStoreListBinding
import com.fhanafi.pitjarus.utils.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StoreListFragment : Fragment() {
    private var _binding: FragmentStoreListBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: StoreViewModel by viewModels()
    private val adapter = StoreAdapter {
        findNavController().navigate(StoreListFragmentDirections.actionStoreListFragmentToStoreDetailFragment(it.id))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()
        setupRecyclerView()
        observeState()
        binding.searchView.queryHint = getString(R.string.search_store)
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.search(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText.orEmpty())
                return true
            }
        })
        binding.fabRefresh.setOnClickListener { viewModel.refresh() }
    }

    private fun setupToolbar() = with(binding.toolbar) {
        setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        setNavigationContentDescription(R.string.navigate_back)
        setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() = with(binding.recyclerStores) {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = this@StoreListFragment.adapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Success -> adapter.submitList(state.data)
                        UiState.Empty -> adapter.submitList(emptyList())
                        is UiState.Error -> android.widget.Toast.makeText(requireContext(), state.message, android.widget.Toast.LENGTH_SHORT).show()
                        is UiState.Unauthorized -> android.widget.Toast.makeText(requireContext(), state.message, android.widget.Toast.LENGTH_SHORT).show()
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
