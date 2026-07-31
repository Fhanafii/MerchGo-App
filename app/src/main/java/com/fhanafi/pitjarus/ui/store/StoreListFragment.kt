package com.fhanafi.pitjarus.ui.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fhanafi.pitjarus.R
import com.fhanafi.pitjarus.databinding.FragmentStoreListBinding
import com.fhanafi.pitjarus.ui.model.StoreUiModel

class StoreListFragment : Fragment() {
    private var _binding: FragmentStoreListBinding? = null
    private val binding get() = requireNotNull(_binding)
    private lateinit var viewModel: StoreViewModel
    private val adapter = StoreAdapter {
        findNavController().navigate(StoreListFragmentDirections.actionStoreListFragmentToStoreDetailFragment())
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
        viewModel = ViewModelProvider(this)[StoreViewModel::class.java]
        setupToolbar()
        setupRecyclerView()
        binding.searchView.queryHint = getString(R.string.search_store)
        binding.fabRefresh.setOnClickListener { adapter.submitList(dummyStores()) }
    }

    private fun setupToolbar() = with(binding.toolbar) {
        setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        setNavigationContentDescription(R.string.navigate_back)
        setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() = with(binding.recyclerStores) {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = this@StoreListFragment.adapter
        this@StoreListFragment.adapter.submitList(dummyStores())
    }

    private fun dummyStores(): List<StoreUiModel> {
        return listOf(
            StoreUiModel(
                1,
                getString(R.string.dummy_store_name),
                getString(R.string.dummy_store_code),
                getString(R.string.dummy_store_address)
            ),
            StoreUiModel(
                2,
                getString(R.string.dummy_store_two_name),
                getString(R.string.dummy_store_two_code),
                getString(R.string.dummy_store_two_address)
            ),
            StoreUiModel(
                3,
                getString(R.string.dummy_store_three_name),
                getString(R.string.dummy_store_three_code),
                getString(R.string.dummy_store_three_address)
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
