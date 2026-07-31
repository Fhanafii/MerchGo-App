package com.fhanafi.pitjarus.ui.promo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fhanafi.pitjarus.R
import com.fhanafi.pitjarus.databinding.BottomSheetAddPromoBinding
import com.fhanafi.pitjarus.databinding.FragmentPromoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class PromoFragment : Fragment() {
    private var _binding: FragmentPromoBinding? = null
    private val binding get() = requireNotNull(_binding)
    private lateinit var viewModel: PromoViewModel
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
        viewModel = ViewModelProvider(this)[PromoViewModel::class.java]
        setupToolbar()
        setupRecyclerView()
        binding.fabAddPromo.setOnClickListener { showAddPromoDialog() }
    }

    private fun setupToolbar() = with(binding.toolbar) {
        setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        setNavigationContentDescription(R.string.navigate_back)
        setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() = with(binding.recyclerPromos) {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = this@PromoFragment.adapter
        this@PromoFragment.adapter.submitList(emptyList())
        binding.emptyState.root.visibility = View.VISIBLE
    }

    private fun showAddPromoDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetAddPromoBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)
        sheetBinding.buttonCancel.setOnClickListener { dialog.dismiss() }
        sheetBinding.buttonSave.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
