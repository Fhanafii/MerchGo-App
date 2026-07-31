package com.fhanafi.pitjarus.ui.promo

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
import com.fhanafi.pitjarus.databinding.BottomSheetAddPromoBinding
import com.fhanafi.pitjarus.databinding.FragmentPromoBinding
import com.fhanafi.pitjarus.utils.UiState
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PromoFragment : Fragment() {
    private var _binding: FragmentPromoBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: PromoViewModel by viewModels()
    private val args: PromoFragmentArgs by navArgs()
    private val adapter = PromoAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPromoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()
        setupRecyclerView()
        observeState()
        viewModel.observePromos(args.storeId)
        binding.fabAddPromo.setOnClickListener { showAddPromoDialog() }
        binding.buttonSubmitPromo.setOnClickListener { viewModel.submitReport(args.storeId) }
    }

    private fun setupToolbar() = with(binding.toolbar) {
        setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        setNavigationContentDescription(R.string.navigate_back)
        setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() = with(binding.recyclerPromos) {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = this@PromoFragment.adapter
    }

    private fun showAddPromoDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetAddPromoBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)
        sheetBinding.buttonCancel.setOnClickListener { dialog.dismiss() }
        sheetBinding.buttonSave.setOnClickListener {
            val error = viewModel.addPromo(
                args.storeId,
                sheetBinding.inputProductName.editText?.text?.toString().orEmpty(),
                sheetBinding.inputNormalPrice.editText?.text?.toString().orEmpty(),
                sheetBinding.inputPromoPrice.editText?.text?.toString().orEmpty()
            )
            if (error == null) {
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                adapter.submitList(state.data)
                                binding.emptyState.root.visibility = View.GONE
                            }
                            UiState.Empty -> {
                                adapter.submitList(emptyList())
                                binding.emptyState.root.visibility = View.VISIBLE
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            is UiState.Unauthorized -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            else -> Unit
                        }
                    }
                }
                launch {
                    viewModel.submitState.collect { state ->
                        binding.buttonSubmitPromo.isEnabled = state !is UiState.Loading
                        if (state is UiState.Success) Toast.makeText(requireContext(), "Promo report berhasil dikirim", Toast.LENGTH_SHORT).show()
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
