package com.fhanafi.pitjarus.ui.promo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.fhanafi.pitjarus.ui.model.ProductUiModel
import com.fhanafi.pitjarus.utils.UiState
import com.fhanafi.pitjarus.utils.hideGlobalLoading
import com.fhanafi.pitjarus.utils.showGlobalLoading
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PromoFragment : Fragment() {
    private var _binding: FragmentPromoBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: PromoViewModel by viewModels()
    private val args: PromoFragmentArgs by navArgs()
    private val adapter = PromoAdapter()
    private var addPromoDialog: BottomSheetDialog? = null
    private var addPromoBinding: BottomSheetAddPromoBinding? = null

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
        val products = viewModel.products.value
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetAddPromoBinding.inflate(layoutInflater)
        addPromoDialog = dialog
        addPromoBinding = sheetBinding
        dialog.setContentView(sheetBinding.root)
        dialog.setOnDismissListener {
            addPromoDialog = null
            addPromoBinding = null
        }
        sheetBinding.dropdownProduct.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                products.map { it.toPromoDisplayName() }
            )
        )
        sheetBinding.dropdownProduct.threshold = 1
        sheetBinding.dropdownProduct.inputType = android.text.InputType.TYPE_CLASS_TEXT
        sheetBinding.textNoProducts.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
        sheetBinding.buttonSave.isEnabled = products.isNotEmpty()
        sheetBinding.buttonCancel.setOnClickListener { dialog.dismiss() }
        sheetBinding.buttonSave.setOnClickListener {
            val error = viewModel.addPromo(
                args.storeId,
                sheetBinding.dropdownProduct.text?.toString().orEmpty(),
                sheetBinding.inputNormalPrice.editText?.text?.toString().orEmpty(),
                sheetBinding.inputPromoPrice.editText?.text?.toString().orEmpty()
            )
            if (error == null) {
                Unit
            } else {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
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
                                hideGlobalLoading()
                            }
                            UiState.Empty -> {
                                adapter.submitList(emptyList())
                                binding.emptyState.root.visibility = View.VISIBLE
                                hideGlobalLoading()
                            }
                            UiState.Loading -> showGlobalLoading("Loading promos...")
                            is UiState.Error -> {
                                hideGlobalLoading()
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            }
                            is UiState.Unauthorized -> {
                                hideGlobalLoading()
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            }
                            UiState.Idle -> Unit
                        }
                    }
                }
                launch {
                    viewModel.submitState.collect { state ->
                        binding.buttonSubmitPromo.isEnabled = state !is UiState.Loading
                        binding.fabAddPromo.isEnabled = state !is UiState.Loading
                        if (state is UiState.Loading) showGlobalLoading("Saving promo...")
                        if (state !is UiState.Loading) hideGlobalLoading()
                        if (state is UiState.Success) Snackbar.make(binding.root, "Promo report berhasil dikirim", Snackbar.LENGTH_SHORT).show()
                        if (state is UiState.Error) Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                    }
                }
                launch {
                    viewModel.addState.collect { state ->
                        val saving = state is UiState.Loading
                        updateAddPromoSheetLoading(saving)
                        when (state) {
                            is UiState.Success -> {
                                addPromoDialog?.dismiss()
                                Snackbar.make(binding.root, "Promo berhasil ditambahkan", Snackbar.LENGTH_SHORT).show()
                                viewModel.clearAddState()
                            }
                            is UiState.Error -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun updateAddPromoSheetLoading(loading: Boolean) {
        addPromoDialog?.setCancelable(!loading)
        addPromoBinding?.apply {
            buttonSave.isEnabled = !loading && viewModel.products.value.isNotEmpty()
            buttonCancel.isEnabled = !loading
            inputProductName.isEnabled = !loading
            inputNormalPrice.isEnabled = !loading
            inputPromoPrice.isEnabled = !loading
            buttonSave.text = getString(if (loading) R.string.saving_promo else R.string.save)
        }
    }

    private fun ProductUiModel.toPromoDisplayName(): String {
        return "${name} - ${barcode}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
