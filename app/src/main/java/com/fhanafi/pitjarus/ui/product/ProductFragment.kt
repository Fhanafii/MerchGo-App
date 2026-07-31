package com.fhanafi.pitjarus.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fhanafi.pitjarus.R
import com.fhanafi.pitjarus.databinding.FragmentProductBinding
import com.fhanafi.pitjarus.utils.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductFragment : Fragment() {
    private var _binding: FragmentProductBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: ProductViewModel by viewModels()
    private val args: ProductFragmentArgs by navArgs()
    private val adapter = ProductAdapter { product, available ->
        viewModel.updateAvailability(args.storeId, product.id, available)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()
        setupRecyclerView()
        observeState()
        viewModel.loadProducts(args.storeId)
        binding.buttonSubmitProduct.setOnClickListener { viewModel.submitReport(args.storeId) }
    }

    private fun setupToolbar() = with(binding.toolbar) {
        setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        setNavigationContentDescription(R.string.navigate_back)
        setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() = with(binding.recyclerProducts) {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = this@ProductFragment.adapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is UiState.Success -> adapter.submitList(state.data)
                            UiState.Empty -> adapter.submitList(emptyList())
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            is UiState.Unauthorized -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            else -> Unit
                        }
                    }
                }
                launch {
                    viewModel.submitState.collect { state ->
                        binding.buttonSubmitProduct.isEnabled = state !is UiState.Loading
                        if (state is UiState.Success) Toast.makeText(requireContext(), "Product report berhasil dikirim", Toast.LENGTH_SHORT).show()
                        if (state is UiState.Error) Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
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
