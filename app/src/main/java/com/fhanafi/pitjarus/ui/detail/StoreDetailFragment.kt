package com.fhanafi.pitjarus.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fhanafi.pitjarus.R
import com.fhanafi.pitjarus.databinding.FragmentStoreDetailBinding

class StoreDetailFragment : Fragment() {
    private var _binding: FragmentStoreDetailBinding? = null
    private val binding get() = requireNotNull(_binding)
    private lateinit var viewModel: StoreDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[StoreDetailViewModel::class.java]
        setupToolbar()
        bindStoreInformation()
        binding.cardProduct.setOnClickListener {
            findNavController().navigate(StoreDetailFragmentDirections.actionStoreDetailFragmentToProductFragment())
        }
        binding.cardPromo.setOnClickListener {
            findNavController().navigate(StoreDetailFragmentDirections.actionStoreDetailFragmentToPromoFragment())
        }
    }

    private fun setupToolbar() = with(binding.toolbar) {
        setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        setNavigationContentDescription(R.string.navigate_back)
        setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun bindStoreInformation() = with(binding) {
        textStoreName.text = getString(R.string.dummy_store_name)
        textStoreCode.text = getString(R.string.dummy_store_code)
        textStoreAddress.text = getString(R.string.dummy_store_address)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
