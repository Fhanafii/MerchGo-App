package com.fhanafi.pitjarus.ui.detail

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
import androidx.navigation.fragment.navArgs
import com.fhanafi.pitjarus.R
import com.fhanafi.pitjarus.databinding.FragmentStoreDetailBinding
import com.fhanafi.pitjarus.utils.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StoreDetailFragment : Fragment() {
    private var _binding: FragmentStoreDetailBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: StoreDetailViewModel by viewModels()
    private val args: StoreDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()
        observeState()
        viewModel.loadStore(args.storeId)
        binding.cardProduct.setOnClickListener {
            findNavController().navigate(StoreDetailFragmentDirections.actionStoreDetailFragmentToProductFragment(args.storeId))
        }
        binding.cardPromo.setOnClickListener {
            findNavController().navigate(StoreDetailFragmentDirections.actionStoreDetailFragmentToPromoFragment(args.storeId))
        }
    }

    private fun setupToolbar() = with(binding.toolbar) {
        setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        setNavigationContentDescription(R.string.navigate_back)
        setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state is UiState.Success) {
                        binding.textStoreName.text = state.data.name
                        binding.textStoreCode.text = state.data.code
                        binding.textStoreAddress.text = state.data.address
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
