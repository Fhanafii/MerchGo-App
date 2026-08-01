package com.fhanafi.pitjarus.ui.store

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fhanafi.pitjarus.R
import com.fhanafi.pitjarus.databinding.BottomSheetAddStoreBinding
import com.fhanafi.pitjarus.databinding.FragmentStoreListBinding
import com.fhanafi.pitjarus.utils.UiState
import com.fhanafi.pitjarus.utils.hideGlobalLoading
import com.fhanafi.pitjarus.utils.showGlobalLoading
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StoreListFragment : Fragment() {
    private var _binding: FragmentStoreListBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: StoreViewModel by viewModels()
    private var addStoreDialog: BottomSheetDialog? = null
    private var addStoreBinding: BottomSheetAddStoreBinding? = null
    private var pendingCreatedStoreName: String? = null
    private val adapter = StoreAdapter {
        findNavController().navigate(StoreListFragmentDirections.actionStoreListFragmentToStoreDetailFragment(it.id))
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showAddStoreDialogWithCurrentLocation()
        } else {
            android.widget.Toast.makeText(requireContext(), "Izin lokasi dibutuhkan untuk menambah store", android.widget.Toast.LENGTH_LONG).show()
        }
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
        binding.fabRefresh.setOnClickListener { requestStoreLocation() }
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
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                adapter.submitList(state.data) {
                                    pendingCreatedStoreName?.let { storeName ->
                                        val index = state.data.indexOfFirst { it.name == storeName }
                                        if (index >= 0) binding.recyclerStores.smoothScrollToPosition(index)
                                        pendingCreatedStoreName = null
                                    }
                                }
                                hideGlobalLoading()
                            }
                            UiState.Empty -> {
                                adapter.submitList(emptyList())
                                hideGlobalLoading()
                            }
                            UiState.Loading -> showGlobalLoading("Loading stores...")
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
                    viewModel.createState.collect { state ->
                        val saving = state is UiState.Loading
                        updateAddStoreSheetLoading(saving)
                        when (state) {
                            is UiState.Success -> {
                                addStoreDialog?.dismiss()
                                Snackbar.make(binding.root, "Store created successfully.", Snackbar.LENGTH_SHORT).show()
                                viewModel.refresh()
                                viewModel.clearCreateState()
                            }
                            is UiState.Error -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            is UiState.Unauthorized -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun requestStoreLocation() {
        if (hasLocationPermission()) {
            showAddStoreDialogWithCurrentLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showAddStoreDialogWithCurrentLocation() {
        fetchCurrentLocation { location ->
            if (!isAdded) return@fetchCurrentLocation
            if (location == null) {
                android.widget.Toast.makeText(requireContext(), "Lokasi GPS belum tersedia", android.widget.Toast.LENGTH_SHORT).show()
                return@fetchCurrentLocation
            }
            showAddStoreDialog(location)
        }
    }

    private fun showAddStoreDialog(location: Location) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetAddStoreBinding.inflate(layoutInflater)
        addStoreDialog = dialog
        addStoreBinding = sheetBinding
        dialog.setContentView(sheetBinding.root)
        dialog.setOnDismissListener {
            addStoreDialog = null
            addStoreBinding = null
        }
        sheetBinding.textCurrentLocation.text = getString(
            R.string.store_location_format,
            location.latitude,
            location.longitude
        )
        sheetBinding.buttonCancel.setOnClickListener { dialog.dismiss() }
        sheetBinding.buttonSave.setOnClickListener {
            val storeName = sheetBinding.inputStoreName.editText?.text?.toString().orEmpty().trim()
            val error = viewModel.createStore(
                sheetBinding.inputStoreCode.editText?.text?.toString().orEmpty(),
                storeName,
                sheetBinding.inputStoreAddress.editText?.text?.toString().orEmpty(),
                location.latitude,
                location.longitude
            )
            if (error == null) {
                pendingCreatedStoreName = storeName
            } else {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun updateAddStoreSheetLoading(loading: Boolean) {
        addStoreDialog?.setCancelable(!loading)
        addStoreBinding?.apply {
            buttonSave.isEnabled = !loading
            buttonCancel.isEnabled = !loading
            inputStoreCode.isEnabled = !loading
            inputStoreName.isEnabled = !loading
            inputStoreAddress.isEnabled = !loading
            buttonSave.text = getString(if (loading) R.string.saving_store else R.string.save)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation(onLocation: (Location?) -> Unit) {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!hasLocationPermission()) {
            onLocation(null)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(
                LocationManager.GPS_PROVIDER,
                CancellationSignal(),
                requireContext().mainExecutor
            ) { gpsLocation ->
                if (gpsLocation != null) {
                    onLocation(gpsLocation)
                } else {
                    locationManager.getCurrentLocation(
                        LocationManager.NETWORK_PROVIDER,
                        CancellationSignal(),
                        requireContext().mainExecutor
                    ) { networkLocation -> onLocation(networkLocation) }
                }
            }
            return
        }
        onLocation(getLastKnownLocation(locationManager))
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(locationManager: LocationManager): Location? {
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
