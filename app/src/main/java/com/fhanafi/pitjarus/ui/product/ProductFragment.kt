package com.fhanafi.pitjarus.ui.product

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.fhanafi.pitjarus.R
import com.fhanafi.pitjarus.databinding.BottomSheetAddProductBinding
import com.fhanafi.pitjarus.databinding.FragmentProductBinding
import com.fhanafi.pitjarus.utils.UiState
import com.fhanafi.pitjarus.utils.hideGlobalLoading
import com.fhanafi.pitjarus.utils.showGlobalLoading
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductFragment : Fragment() {
    private var _binding: FragmentProductBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: ProductViewModel by viewModels()
    private val args: ProductFragmentArgs by navArgs()
    private var addProductDialog: BottomSheetDialog? = null
    private var addProductBinding: BottomSheetAddProductBinding? = null
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
        binding.fabAddProduct.setOnClickListener { showAddProductDialog() }
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
                            is UiState.Success -> {
                                adapter.submitList(state.data)
                                hideGlobalLoading()
                            }
                            UiState.Empty -> {
                                adapter.submitList(emptyList())
                                hideGlobalLoading()
                            }
                            UiState.Loading -> showGlobalLoading("Loading products...")
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
                        binding.buttonSubmitProduct.isEnabled = state !is UiState.Loading
                        binding.fabAddProduct.isEnabled = state !is UiState.Loading
                        if (state is UiState.Loading) showGlobalLoading("Saving availability...")
                        if (state !is UiState.Loading) hideGlobalLoading()
                        if (state is UiState.Success) Snackbar.make(binding.root, "Product report berhasil dikirim", Snackbar.LENGTH_SHORT).show()
                        if (state is UiState.Error) Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                    }
                }
                launch {
                    viewModel.createState.collect { state ->
                        val saving = state is UiState.Loading
                        updateAddProductSheetLoading(saving)
                        when (state) {
                            is UiState.Success -> {
                                addProductDialog?.dismiss()
                                Snackbar.make(binding.root, "Produk berhasil ditambahkan", Snackbar.LENGTH_SHORT).show()
                                viewModel.clearCreateState()
                            }
                            is UiState.Error -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            is UiState.Unauthorized -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            else -> Unit
                        }
                    }
                }
                launch {
                    viewModel.availabilityLoadingIds.collect { adapter.submitLoadingIds(it) }
                }
            }
        }
    }

    private fun showAddProductDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetAddProductBinding.inflate(layoutInflater)
        addProductDialog = dialog
        addProductBinding = sheetBinding
        dialog.setContentView(sheetBinding.root)
        dialog.setOnDismissListener {
            addProductDialog = null
            addProductBinding = null
        }
        sheetBinding.buttonCancel.setOnClickListener { dialog.dismiss() }
        sheetBinding.buttonSave.setOnClickListener {
            val error = viewModel.createAndAssignProduct(
                args.storeId,
                sheetBinding.inputProductName.editText?.text?.toString().orEmpty(),
                sheetBinding.inputBarcode.editText?.text?.toString().orEmpty(),
                sheetBinding.inputSku.editText?.text?.toString().orEmpty(),
                sheetBinding.inputSize.editText?.text?.toString().orEmpty(),
                sheetBinding.inputPrice.editText?.text?.toString().orEmpty()
            )
            if (error == null) {
                Unit
            } else {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun updateAddProductSheetLoading(loading: Boolean) {
        addProductDialog?.setCancelable(!loading)
        addProductBinding?.apply {
            buttonSave.isEnabled = !loading
            buttonCancel.isEnabled = !loading
            inputProductName.isEnabled = !loading
            inputBarcode.isEnabled = !loading
            inputSku.isEnabled = !loading
            inputSize.isEnabled = !loading
            inputPrice.isEnabled = !loading
            buttonSave.text = getString(if (loading) R.string.creating_product else R.string.save)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
