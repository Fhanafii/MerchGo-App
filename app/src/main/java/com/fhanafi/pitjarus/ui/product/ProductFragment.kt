package com.fhanafi.pitjarus.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fhanafi.pitjarus.R
import com.fhanafi.pitjarus.databinding.FragmentProductBinding
import com.fhanafi.pitjarus.ui.model.ProductUiModel

class ProductFragment : Fragment() {
    private var _binding: FragmentProductBinding? = null
    private val binding get() = requireNotNull(_binding)
    private lateinit var viewModel: ProductViewModel
    private val adapter = ProductAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() = with(binding.toolbar) {
        setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        setNavigationContentDescription(R.string.navigate_back)
        setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() = with(binding.recyclerProducts) {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = this@ProductFragment.adapter
        this@ProductFragment.adapter.submitList(dummyProducts())
    }

    private fun dummyProducts(): List<ProductUiModel> {
        return listOf(
            ProductUiModel(
                1,
                getString(R.string.dummy_product_one),
                getString(R.string.dummy_product_one_barcode),
                true
            ),
            ProductUiModel(
                2,
                getString(R.string.dummy_product_two),
                getString(R.string.dummy_product_two_barcode),
                false
            ),
            ProductUiModel(
                3,
                getString(R.string.dummy_product_three),
                getString(R.string.dummy_product_three_barcode),
                true
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
